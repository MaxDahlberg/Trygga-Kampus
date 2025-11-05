@file:Suppress("unused")

package com.example.tryggakampus.presentation.authentication.loginPage

import com.example.tryggakampus.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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
    fun `onForgotPassword with invalid email sets error`() = runTest {
        viewModel.onEmailChange("invalid")

        viewModel.onForgotPassword()

        assertEquals("Please enter a valid email address.", viewModel.error?.message)
    }

    @Test
    fun `clearError clears error`() = runTest {
        // Cause an error first
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("")
        viewModel.onRequestLogin()

        viewModel.clearError()

        assertNull(viewModel.error)
    }

    @Test
    fun `togglePasswordVisibility toggles visibility`() = runTest {
        assertTrue(!viewModel.isPasswordVisible)

        viewModel.togglePasswordVisibility()

        assertTrue(viewModel.isPasswordVisible)
    }
}