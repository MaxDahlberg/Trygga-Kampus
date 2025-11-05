@file:Suppress("unused")
package com.example.tryggakampus.presentation.authentication.registerPage

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
@Config(sdk = [28])
class RegisterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val viewModel = RegisterViewModel()

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
    fun `clearError clears error`() = runTest {
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("")
        viewModel.onRequestSignUp()

        viewModel.clearError()

        assertNull(viewModel.error)
    }
}