

// Load environment variables from .env file
require('dotenv').config();

const express = require('express');
const bcrypt = require('bcrypt');
const { Pool } = require('pg');
const crypto = require('crypto');
const nodemailer = require('nodemailer');
const { console } = require('inspector');
const jwt = require('jsonwebtoken');

const pool = new Pool({
    user: process.env.DB_USER,
    host: process.env.DB_HOST,
    database: process.env.DB_NAME,
    password: process.env.DB_PASSWORD,
    port: parseInt(process.env.DB_PORT || '5432', 10),
});

// Middleware to authenticate JWT tokens
const authenticateJWT = (req, res, next) => {
    const token = req.headers['authorization']?.split(' ')[1]; // Get token from Authorization header
    if (!token) {
        console.error('Access token is missing');
        return res.status(401).json({ message: 'Access token is required' });
    }

    jwt.verify(token, process.env.JWT_SECRET, (err, user) => {
        if (err) {
            return res.status(403).json({ message: 'Invalid or expired token' });
        }
        req.user = user; // Attach user info to request object
        console.log(`Authenticated user: ${user.username} (ID: ${user.userId})`);
        next();
    });
};

// Test DB connection (optional but recommended)
pool.query('SELECT NOW()', (err, res) => {
    if (err) {
        console.error('Database connection error:', err.stack);
    } else {
        console.log('Database connected successfully:', res.rows[0].now);
    }
});


const transporter = nodemailer.createTransport({
    host: process.env.EMAIL_HOST,
    port: parseInt(process.env.EMAIL_PORT || '587', 10),
    secure: process.env.EMAIL_SECURE === 'true', // true for 465, false for other ports
    auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASSWORD,
    },
});

transporter.verify((error, success) => {
    if (error) {
        console.error('Email transporter configuration error:', error);
    } else {
        console.log('Email transporter is ready to send messages');
    }
});

// Create an instance of the Express application
const app = express();
app.use(express.json());
const PORT = process.env.PORT || 3000;

app.get('/', (req, res) => {
    res.status(200).send('Hello from the server!');
});

// --- Signup Route ---
// Define the POST route for user signup
app.post('/api/auth/signup', async (req, res) => {
    const client = await pool.connect();
    try {
        // Call the signup function
        const { username, email, password, address, gender } = req.body;
        // Validate the input
        if (!username || !email || !password) {
            return res.status(400).json({ message: 'Username, email and password are required' });
        }
        const checkUserSql = 'SELECT * FROM users WHERE username = $1 OR email = $2';
        const checkResult = await client.query(checkUserSql, [username, email]);
        if (checkResult.rows.length > 0) {
            return res.status(400).json({ message: 'Username or email already exists' });
        }
        // Hash the password
        const hashedPassword = await bcrypt.hash(password, 10);
        // Insert the new user into the database
        const insertUserSql = 'INSERT INTO users (username, email, password_hash, address, gender) VALUES ($1, $2, $3, $4, $5) RETURNING *';
        const insertResult = await client.query(insertUserSql, [username, email, hashedPassword, address, gender]);
        console.log('User created successfully:', insertResult.rows[0]);

        // Send a success response
        res.status(201).json({
            message: 'User created successfully',
            user: {
                id: insertResult.rows[0].id,
                username: insertResult.rows[0].username,
                created_at: insertResult.rows[0].created_at,
            }
        });

    } catch (error) {
        console.error('Error during signup:', error);
        res.status(500).json({ message: 'Internal server error' });
    } finally {
        client.release();
    }

});

// --- Login Route ---
app.post('/api/auth/login', async (req, res) => {
    const client = await pool.connect();
    try {
        // 1. Get credentials from request body
        const { usernameOrEmail, password } = req.body;

        // 2. Basic Input Validation
        if (!usernameOrEmail || !password) {
            return res.status(400).json({ error: 'Username/Email and password are required.' });
        }

        // 3. Find user by username or email
        const findUserSql = 'SELECT * FROM users WHERE username = $1 OR email = $1'; // Use $1 for both possibilities
        const userResult = await client.query(findUserSql, [usernameOrEmail]);

        if (userResult.rows.length === 0) {
            // User not found - send 401 Unauthorized for security
            console.log(`Login attempt failed: User not found for ${usernameOrEmail}`);
            return res.status(401).json({ error: 'Invalid credentials.' });
        }

        const user = userResult.rows[0];

        // 4. Compare provided password with stored hash
        const isPasswordMatch = await bcrypt.compare(password, user.password_hash);

        if (!isPasswordMatch) {
            // Password doesn't match - send 401 Unauthorized
            console.log(`Login attempt failed: Invalid password for ${user.username}`);
            return res.status(401).json({ error: 'Invalid credentials.' });
        }

        // 5. Password matches - Generate JWT
        const jwtPayload = {
            userId: user.id,
            username: user.username
            // Add other non-sensitive info if needed (e.g., roles)
        };

        const jwtSecret = process.env.JWT_SECRET;
        if (!jwtSecret) {
            // Log a critical error if the secret is missing
            console.error("FATAL ERROR: JWT_SECRET is not defined in .env file!");
            // Don't expose internal errors to the client
            return res.status(500).json({ error: "Server configuration error." });
        }

        // Generate the token with an expiration time
        const token = jwt.sign(
            jwtPayload,
            jwtSecret,
            { expiresIn: '1h' } // Token expiration time (e.g., 1 hour, '7d' for 7 days)
        );

        console.log(`Login successful for user: ${user.username}`);

        // 6. Send Success Response with Token
        res.status(200).json({
            message: 'Login successful!',
            token: token,
            // Optionally include some user details if needed by the app immediately
            user: { id: user.id, username: user.username, email: user.email }
        });

    } catch (error) {
        // 7. Handle Errors
        console.error('Error during login:', error);
        res.status(500).json({ error: 'An unexpected error occurred during login.' });
    } finally {
        // 8. Release the client back to the pool ALWAYS
        if (client) client.release(); // Release client in finally block
    }
});
// ------------------

// --- Forgot Password Route ---
app.post('/api/auth/forgot-password', async (req, res) => {
    const client = await pool.connect();
    try {
        const { email } = req.body;
        if (!email) {
            return res.status(400).json({ message: 'Email is required' });
        }
        console.log(`Password reset requested for email: ${email}`);
        // Check if user exists
        const findUserSql = 'SELECT * FROM users WHERE email = $1';
        const userResult = await client.query(findUserSql, [email]);

        // Note in security, we do not reveal whether the email exists in the system
        // to prevent user enumeration attacks
        if (userResult.rows.length === 0) {
            console.log(`Password reset attempt failed: User not found for email ${email}`);
            return res.status(404).json({ message: 'The require is not found' });
        }

        // Here you would typically generate a reset token and send an email
        // For simplicity, we'll just return a success message
        const user = userResult.rows[0];
        // Generate a reset token (in a real app, you would store this in the database)
        const resetToken = crypto.randomBytes(32).toString('hex');// Generate a random token 64 characters long

        // Set timeout for the token (e.g., 1 hour)
        const expireAt = new Date(Date.now() + 60 * 60 * 1000); // 1 hour from now

        // Save the reset token and expiration in the database
        const insertTokenSql = 'INSERT INTO password_resets (user_id, token, expires_at) VALUES ($1, $2, $3)';
        await client.query(insertTokenSql, [user.id, resetToken, expireAt]);

        const resetLink = `${process.env.FRONTEND_URL || 'http://localhost:3000'}/reset-password?token=${resetToken}`;
        const mailOptions = {
            from: process.env.EMAIL_USER,
            to: user.email,
            subject: 'Password Reset Request',
            text: `You requested a password reset. Click the link below to reset your password:\n\n${resetLink}\n\nThis link will expire in 1 hour.`,
        };
        
        try {
            // Send the email with the reset link
            let info = await transporter.sendMail(mailOptions);
            console.log('Password reset email sent:, ${user.email}: ${info.messageId}');
        }
        catch (emailError) {
            console.error('Error sending password reset email:', emailError);
            return res.status(500).json({ message: 'Failed to send password reset email' });
        }       
        // Here you would send the reset link via email when implementing this in a real application
        // For example: `http://yourapp.com/reset-password?token=${resetToken}`
        // For now, we will just log it to the console
        console.log(`Password reset token for user ${user.username}: ${resetToken}`);
        res.status(200).json({ message: 'Password reset link sent to your email' });

    } catch (error) {
        console.error('Error during forgot password:', error);
        res.status(500).json({ message: 'Internal server error' });
    } finally {
        client.release();
    }
});

// --- Reset Password Route ---
app.post('/api/auth/reset-password', async (req, res) => {
    const client = await pool.connect();
    try {
        const { token, newPassword } = req.body;
        if (!token || !newPassword) {
            return res.status(400).json({ message: 'Token and new password are required' });
        }

        if (newPassword.length < 6) {
            return res.status(400).json({ message: 'New password must be at least 6 characters long' });
        }
        // Find the reset token in the database
        const findTokenSql = 'SELECT * FROM password_resets WHERE token = $1';
        const tokenResult = await client.query(findTokenSql, [token]);

        if (tokenResult.rows.length === 0) {
            return res.status(400).json({ message: 'Invalid or expired reset token' });
        }

        const resetEntry = tokenResult.rows[0];
        // Check if the token has expired
        if (new Date(resetEntry.expires_at) < new Date()) {
            await client.query('DELETE FROM password_resets WHERE token = $1', [token]); 
            return res.status(400).json({ message: 'Reset token has expired' });
        }
        // Hash the new password
        const hashedPassword = await bcrypt.hash(newPassword, 10);
        // Update the user's password
        const updateUserSql = 'UPDATE users SET password_hash = $1 WHERE id = $2';
        await client.query(updateUserSql, [hashedPassword, resetEntry.user_id]);
        
        // Optionally delete the reset entry after use
        const deleteTokenSql = 'DELETE FROM password_resets WHERE token = $1';
        await client.query(deleteTokenSql, [token]);

        console.log(`Password reset successful for user ID ${resetEntry.user_id}`);
        res.status(200).json({ message: 'Password has been reset successfully' });

    } catch (error) {
        console.error('Error during reset password:', error);
        res.status(500).json({ message: 'Internal server error' });
    } finally {
        client.release();
    }
});


// --- Protected Route ---
// -- Profile Route --
app.get('/api/profile/me', authenticateJWT, async (req, res) => {
    const client = await pool.connect();
    try {
        // Get the user ID from the JWT
        const userId = req.user.userId;

        // Fetch user profile from the database
        const findUserSql = 'SELECT id, username, email, address, gender, created_at FROM users WHERE id = $1';
        const userResult = await client.query(findUserSql, [userId]);

        if (userResult.rows.length === 0) {
            return res.status(404).json({ message: 'User not found' });
        }

        const userProfile = userResult.rows[0];
        res.status(200).json({ user: userProfile });

    } catch (error) {
        console.error('Error fetching user profile:', error);
        res.status(500).json({ message: 'Internal server error' });
    } finally {
        client.release();
    }
});




// --- Start the server ---
app.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`);
});