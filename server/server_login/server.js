

// Load environment variables from .env file
require('dotenv').config();

const express = require('express');
const bcrypt = require('bcrypt');
const { Pool } = require('pg');

const pool = new Pool({
    user: process.env.DB_USER,
    host: process.env.DB_HOST,
    database: process.env.DB_NAME,
    password: process.env.DB_PASSWORD,
    port: parseInt(process.env.DB_PORT || '5432', 10),
});

// Test DB connection (optional but recommended)
pool.query('SELECT NOW()', (err, res) => {
    if (err) {
        console.error('Database connection error:', err.stack);
    } else {
        console.log('Database connected successfully:', res.rows[0].now);
    }
});

// Create an instance of the Express application
const app = express();
app.use(express.json());

const PORT = process.env.PORT || 3000;

app.get('/', (req, res) => {
    res.send('Hello from the server!');
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
            message: 'User created successfully', user: {
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
/*
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
*/
        console.log(`Login successful for user: ${user.username}`);

        // 6. Send Success Response with Token
        res.status(200).json({
            message: 'Login successful!',
            //token: token
            // Optionally include some user details if needed by the app immediately
            // user: { id: user.id, username: user.username, email: user.email }
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

// --- Start the server ---
app.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`);
});