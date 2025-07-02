package com.example.loginappviewmodel.ui.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.loginappviewmodel.data.repository.AuthRepository
import com.example.loginappviewmodel.data.resource.network.dto.LoginRequest
import com.example.loginappviewmodel.data.resource.network.dto.LoginResponse
import com.example.loginappviewmodel.data.resource.network.dto.UserDto
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Use StandardTestDispatcher for coroutine test
    private val dispatcher = StandardTestDispatcher()

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        // Mock AuthService only
        authRepository = mockk(relaxed = true)
        // Init ViewModel with mocked AuthService
        viewModel = LoginViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `onUsernameOrEmailChange updates uiState`() = runTest {
        viewModel.onUsernameOrEmailChange("testuser")
        val state = viewModel.uiState.first()
        Assert.assertEquals("testuser", state.usernameOrEmailInput)
        Assert.assertNull(state.errorMessage)
        Assert.assertNull(state.receivedToken)
    }

    @Test
    fun `onPasswordChange updates uiState`() = runTest {
        viewModel.onPasswordChange("123456")
        val state = viewModel.uiState.first()
        Assert.assertEquals("123456", state.passwordInput)
        Assert.assertNull(state.errorMessage)
        Assert.assertNull(state.receivedToken)
    }

    @Test
    fun `performLogin with blank username or password sets error message`() = runTest {
        viewModel.onUsernameOrEmailChange("")
        viewModel.onPasswordChange("")
        viewModel.performLogin()
        advanceUntilIdle()
        val state = viewModel.uiState.first()
        Assert.assertEquals("Username or email and password are required.", state.errorMessage)
        Assert.assertNull(state.receivedToken)
    }

    @Test
    fun `performLogin with correct credentials updates state with token`() = runTest {
        // Setup
        val loginRequest = LoginRequest("user", "pass")
        val userDto = UserDto(
            id = 1,
            username = "testuser",
            email = "abc@gmail.com",
            address = "123 Main St",
            gender = "Male",
            created_at = "2023-10-01T12:00:00Z",
            )
        val loginResponse = LoginResponse("Login success", "token123", userDto)
        coEvery { authRepository.login(any()) } returns Response.success(loginResponse)
        coEvery { authRepository.saveAuthToken(any()) } just Runs

        viewModel.onUsernameOrEmailChange("user")
        viewModel.onPasswordChange("pass")
        viewModel.performLogin()
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        Assert.assertEquals("token123", state.receivedToken)
        Assert.assertTrue(state.loginSuccess)
        Assert.assertEquals(userDto, state.loggedInUser)
        Assert.assertNull(state.errorMessage)
        Assert.assertFalse(state.isLoading)
    }

    @Test
    fun `performLogin with API error sets error message`() = runTest {
        val errorMsg = "Invalid credentials"
        val errorBody = """{"error":"$errorMsg"}"""
        val responseBody = ResponseBody.create("application/json".toMediaTypeOrNull(), errorBody)
        coEvery { authRepository.login(any()) } returns Response.error(401, responseBody)
        viewModel.onUsernameOrEmailChange("user")
        viewModel.onPasswordChange("wrongpass")
        viewModel.performLogin()
        advanceUntilIdle()
        val state = viewModel.uiState.first()
        Assert.assertEquals(errorMsg, state.errorMessage)
        Assert.assertNull(state.receivedToken)
        Assert.assertFalse(state.loginSuccess)
        Assert.assertFalse(state.isLoading)
    }

    @Test
    fun `onLoginNavigated resets loginSuccess flag`() = runTest {
        // Setup state with loginSuccess true
        viewModel.onUsernameOrEmailChange("user")
        viewModel.onPasswordChange("pass")
        // Simulate login success
        val stateWithSuccess = viewModel.uiState.value.copy(loginSuccess = true)
        viewModel.apply {
            this::class.java.getDeclaredField("_uiState").apply {
                isAccessible = true
                set(viewModel, MutableStateFlow(stateWithSuccess))
            }
        }
        viewModel.onLoginNavigated()
        val state = viewModel.uiState.first()
        Assert.assertFalse(state.loginSuccess)
    }
}