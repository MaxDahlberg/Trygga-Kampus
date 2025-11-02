@file:Suppress("unused")
package com.example.tryggakampus.presentation.authentication.registerPage

import com.example.tryggakampus.domain.repository.AuthRepositoryImpl
import com.example.tryggakampus.domain.repository.AuthResponse
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class RegisterViewModelTest {

    private val mockRepo = mockk<AuthRepositoryImpl>(relaxed = true)
    private val viewModel = RegisterViewModel()

    @Before
    fun setup() {
        mockkStatic(AuthRepositoryImpl::class)
    }

    @After
    fun teardown() {
        unmockkAll()
        unmockkStatic(AuthRepositoryImpl::class)
    }

    @Test
    fun `onEmailChange sets valid for valid email`() = runTest {
        viewModel.onEmailChange("test@example.com")

        assertTrue(viewModel.emailIsValid)
        assertEquals("test@example.com", viewModel.email)
    }

    @Test
    fun `onEmailChange sets invalid for invalid email`() = runTest {
        viewModel.onEmailChange("invalid")

        assertTrue(!viewModel.emailIsValid)
    }

    @Test
    fun `onPasswordChange sets valid for matching regex`() = runTest {
        viewModel.onPasswordChange("Password123!")

        assertTrue(viewModel.passwordIsValid)
        assertEquals("Password123!", viewModel.password)
    }

    @Test
    fun `onPasswordChange sets invalid for non-matching regex`() = runTest {
        viewModel.onPasswordChange("short")

        assertTrue(!viewModel.passwordIsValid)
    }

    @Test
    fun `onRequestSignUp with empty fields sets error`() = runTest {
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("")
        viewModel.onRequestSignUp()

        assertEquals("Email and password cannot be empty", viewModel.error?.message)
    }

    @Test
    fun `onRequestSignUp with invalid email sets error`() = runTest {
        viewModel.onEmailChange("invalid")
        viewModel.onPasswordChange("Password123!")
        viewModel.onRequestSignUp()

        assertEquals("Please enter a valid email address", viewModel.error?.message)
    }

    @Test
    fun `onRequestSignUp with invalid password sets error`() = runTest {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("short")
        viewModel.onRequestSignUp()

        assertEquals("Password does not meet the requirements. It must be 8-20 characters and include an uppercase letter, a number, and a special character.", viewModel.error?.message)
    }

    @Test
    fun `onRequestSignUp with valid input calls repo and handles SUCCESS`() = runTest {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("Password123!")
        coEvery { AuthRepositoryImpl.registerUser(any(), any()) } returns AuthResponse.SignUp.SUCCESS

        viewModel.onRequestSignUp()

        coVerify { AuthRepositoryImpl.registerUser("test@example.com", "Password123!") }
        assertNull(viewModel.error)
    }

    @Test
    fun `onRequestSignUp with EMAIL_TAKEN sets error`() = runTest {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("Password123!")
        coEvery { AuthRepositoryImpl.registerUser(any(), any()) } returns AuthResponse.SignUp.EMAIL_TAKEN

        viewModel.onRequestSignUp()

        assertEquals("This email is already taken", viewModel.error?.message)
    }

    @Test
    fun `onRequestSignUp with FAILURE sets error`() = runTest {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("Password123!")
        coEvery { AuthRepositoryImpl.registerUser(any(), any()) } returns AuthResponse.SignUp.FAILURE

        viewModel.onRequestSignUp()

        assertEquals("Sign up failed. Please check your details.", viewModel.error?.message)
    }

    @Test
    fun `onRequestSignUp with ERROR sets error`() = runTest {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("Password123!")
        coEvery { AuthRepositoryImpl.registerUser(any(), any()) } returns AuthResponse.SignUp.ERROR

        viewModel.onRequestSignUp()

        assertEquals("Something went wrong, please try again later.", viewModel.error?.message)
    }

    @Test
    fun `clearError clears error`() = runTest {
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("")
        viewModel.onRequestSignUp()

        viewModel.clearError()

        assertNull(viewModel.error)
    }
}