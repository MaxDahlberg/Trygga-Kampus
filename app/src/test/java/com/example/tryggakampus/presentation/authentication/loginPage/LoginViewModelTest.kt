@file:Suppress("unused")

package com.example.tryggakampus.presentation.authentication.loginPage

import com.example.tryggakampus.domain.repository.AuthRepositoryImpl
import com.example.tryggakampus.domain.repository.AuthResponse
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test


class LoginViewModelTest {

    private val mockRepo = mockk<AuthRepositoryImpl>(relaxed = true)
    private val viewModel = LoginViewModel()

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
    fun `onRequestLogin with empty fields sets error`() = runTest {
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("")
        viewModel.onRequestLogin()

        assertEquals("Wrong username or password", viewModel.error?.message)
    }

    @Test
    fun `onRequestLogin with valid input calls repo and handles SUCCESS`() = runTest {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("Password123!")
        coEvery { AuthRepositoryImpl.authenticateUser(any(), any()) } returns AuthResponse.SignIn.SUCCESS

        viewModel.onRequestLogin()

        coVerify { AuthRepositoryImpl.authenticateUser("test@example.com", "Password123!") }
        assertNull(viewModel.error)
    }

    @Test
    fun `onRequestLogin with FAILURE sets error`() = runTest {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("Password123!")
        coEvery { AuthRepositoryImpl.authenticateUser(any(), any()) } returns AuthResponse.SignIn.FAILURE

        viewModel.onRequestLogin()

        assertEquals("Wrong username or password", viewModel.error?.message)
    }

    @Test
    fun `onRequestLogin with ERROR sets error`() = runTest {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("Password123!")
        coEvery { AuthRepositoryImpl.authenticateUser(any(), any()) } returns AuthResponse.SignIn.ERROR

        viewModel.onRequestLogin()

        assertEquals("Something went wrong, please try again later.", viewModel.error?.message)
    }

    @Test
    fun `onForgotPassword with invalid email sets error`() = runTest {
        viewModel.onEmailChange("invalid")

        viewModel.onForgotPassword()

        assertEquals("Please enter a valid email address.", viewModel.error?.message)
    }

    @Test
    fun `onForgotPassword with valid email calls repo and sets sent`() = runTest {
        viewModel.onEmailChange("test@example.com")
        coEvery { AuthRepositoryImpl.sendPasswordResetEmail(any()) } returns AuthResponse.PasswordReset.SUCCESS

        viewModel.onForgotPassword()

        coVerify { AuthRepositoryImpl.sendPasswordResetEmail("test@example.com") }
        assertTrue(viewModel.passwordResetEmailSent)
        assertNull(viewModel.error)
    }

    @Test
    fun `onForgotPassword with FAILURE sets error`() = runTest {
        viewModel.onEmailChange("test@example.com")
        coEvery { AuthRepositoryImpl.sendPasswordResetEmail(any()) } returns AuthResponse.PasswordReset.FAILURE

        viewModel.onForgotPassword()

        assertEquals("Failed to send reset email. Please ensure the email is correct.", viewModel.error?.message)
    }

    @Test
    fun `onSignInWithGoogle calls repo and handles success`() = runTest {
        coEvery { AuthRepositoryImpl.signInWithGoogle(any()) } returns AuthResponse.SignIn.SUCCESS

        viewModel.onSignInWithGoogle("fake_token")

        coVerify { AuthRepositoryImpl.signInWithGoogle("fake_token") }
        assertNull(viewModel.error)
    }

    @Test
    fun `onSignInWithGoogle with ERROR sets error`() = runTest {
        coEvery { AuthRepositoryImpl.signInWithGoogle(any()) } returns AuthResponse.SignIn.ERROR

        viewModel.onSignInWithGoogle("fake_token")

        assertEquals("Google sign-in failed.", viewModel.error?.message)
    }

    @Test
    fun `onSignInWithFacebook calls repo and handles success`() = runTest {
        coEvery { AuthRepositoryImpl.signInWithFacebook(any()) } returns AuthResponse.SignIn.SUCCESS

        viewModel.onSignInWithFacebook("fake_token")

        coVerify { AuthRepositoryImpl.signInWithFacebook("fake_token") }
        assertNull(viewModel.error)
    }

    @Test
    fun `onSignInWithFacebook with ERROR sets error`() = runTest {
        coEvery { AuthRepositoryImpl.signInWithFacebook(any()) } returns AuthResponse.SignIn.ERROR

        viewModel.onSignInWithFacebook("fake_token")

        assertEquals("Facebook sign-in failed.", viewModel.error?.message)
    }

    @Test
    fun `clearError clears error`() = runTest {
        // Given
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("")
        viewModel.onRequestLogin()

        viewModel.clearError()

        assertNull(viewModel.error)
    }

    @Test
    fun `dismissPasswordResetMessage clears sent state`() = runTest {
        viewModel.onEmailChange("test@example.com")
        coEvery { AuthRepositoryImpl.sendPasswordResetEmail(any()) } returns AuthResponse.PasswordReset.SUCCESS

        viewModel.onForgotPassword()
        viewModel.dismissPasswordResetMessage()

        coVerify { AuthRepositoryImpl.sendPasswordResetEmail("test@example.com") }
        assertNull(viewModel.error)
    }

    @Test
    fun `togglePasswordVisibility toggles visibility`() = runTest {
        assertTrue(!viewModel.isPasswordVisible)

        viewModel.togglePasswordVisibility()

        assertTrue(viewModel.isPasswordVisible)
    }
}