@file:Suppress("unused")
package com.example.tryggakampus.presentation.profilePage

import com.example.tryggakampus.presentation.authentication.loginPage.AuthError
import com.example.tryggakampus.testing.MainDispatcherRule
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var firebaseAuthStatic: MockedStatic<FirebaseAuth>

    private class TestProfileViewModel : ProfileViewModel() {
        // override nothing but avoid side effects by not triggering init: we can shadow with an empty init
        init {
            // no-op
        }
    }

    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setup() {
        // Mock static FirebaseAuth.getInstance to avoid touching real Firebase
        val mockAuth: FirebaseAuth = mock()
        whenever(mockAuth.currentUser).thenReturn(null)
        firebaseAuthStatic = Mockito.mockStatic(FirebaseAuth::class.java)
        firebaseAuthStatic.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(mockAuth)

        viewModel = TestProfileViewModel()
    }

    @org.junit.After
    fun tearDown() {
        firebaseAuthStatic.close()

    }
    @Test
    fun onHobbyToggleAddsRemovesHobbyFromList() = runTest {
        viewModel.hobbies = mutableSetOf("hobby1")

        viewModel.onHobbyToggle("hobby1")
        assertFalse(viewModel.hobbies.contains("hobby1"))

        viewModel.onHobbyToggle("hobby2")
        assertTrue(viewModel.hobbies.contains("hobby2"))
    }


    @Test
    fun clearErrorClearsAllErrors() = runTest {
        viewModel.hobbiesError = AuthError("h")
        viewModel.usernameError = AuthError("u")
        viewModel.passwordError = AuthError("p")
        viewModel.deleteAccountError = AuthError("d")

        viewModel.clearError()

        assertNull(viewModel.hobbiesError)
        assertNull(viewModel.usernameError)
        assertNull(viewModel.passwordError)
        assertNull(viewModel.deleteAccountError)
    }
}