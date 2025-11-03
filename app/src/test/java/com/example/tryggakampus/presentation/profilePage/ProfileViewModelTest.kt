@file:Suppress("unused")
package com.example.tryggakampus.presentation.profilePage

import android.content.Context
import com.example.tryggakampus.R
import com.example.tryggakampus.domain.model.UserInfoModel
import com.example.tryggakampus.domain.repository.UserInformationRepository
import com.example.tryggakampus.domain.repository.UserInformationRepository.RepositoryResult
import com.example.tryggakampus.domain.repository.UserInformationRepositoryImpl
import com.example.tryggakampus.presentation.authentication.loginPage.AuthError
import com.example.tryggakampus.util.GdprUserDataHelper
import com.example.tryggakampus.util.HobbyList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


@ExperimentalCoroutinesApi
class ProfileViewModelTest {

    private val mockRepo = mockk<UserInformationRepository>(relaxed = true)
    private val mockAuth = mockk<FirebaseAuth>(relaxed = true)
    private val mockUser = mockk<FirebaseUser>(relaxed = true)
    private val mockGdprHelper = mockk<GdprUserDataHelper>(relaxed = true)
    private val viewModel = ProfileViewModel()
    private val context = mockk<Context>(relaxed = true)

    @Before
    fun setup() {
        // Mock Firebase
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "test-uid"
        every { mockUser.email } returns "test@example.com"

        // Mock context for strings
        every { context.getString(R.string.error_update_hobbies) } returns "Error updating hobbies"
        every { context.getString(R.string.error_update_hobbies_detail, any()) } returns "Error updating hobbies: detail"
        every { context.getString(R.string.error_fill_all_fields) } returns "Fill all fields"
        every { context.getString(R.string.error_username_taken) } returns "Username taken"
        every { context.getString(R.string.error_update_username_failed, any()) } returns "Update username failed: detail"
        every { context.getString(R.string.error_invalid_passwords) } returns "Invalid passwords"
        every { context.getString(R.string.error_password_change_failed, any()) } returns "Password change failed: detail"
        every { context.getString(R.string.error_password_required) } returns "Password required"
        every { context.getString(R.string.error_account_deletion_failed, any()) } returns "Deletion failed: detail"

        // Mock GdprHelper
        coEvery { mockGdprHelper.deleteUserData(any()) } just Runs
        coEvery { mockGdprHelper.fetchUserData(any()) } returns "{}"

        // Mock HobbyList (static)
        mockkStatic(HobbyList::class)
        every { HobbyList.allHobbies } returns listOf("hobby1" to 1, "hobby2" to 2)
        every { HobbyList.getDisplayName(any(), any()) } returns "Hobby 1"

        // Mock repo (static)
        mockkObject(UserInformationRepositoryImpl)
    }

    @After
    fun teardown() {
        unmockkAll()
        unmockkStatic(HobbyList::class)
        unmockkObject(UserInformationRepositoryImpl)
    }

    @Test
    fun initLoadsUserInfoAndSetsUsernameEmail() = runTest {
        val fakeUserInfo = UserInfoModel(userId = "test-uid", username = "Test User", hobbies = listOf("hobby1"))
        coEvery { UserInformationRepositoryImpl.getUserInformation(any(), any()) } returns (RepositoryResult.SUCCESS to fakeUserInfo)

        val vm = ProfileViewModel()  // Init triggers load

        assertEquals("Test User", vm.username)
        assertEquals("test@example.com", vm.email)
        assertEquals(mutableSetOf("hobby1"), vm.hobbies)
    }

    @Test
    fun loadAllHobbiesSetsAllHobbiesFromHobbyList() = runTest {
        viewModel.loadAllHobbies(context)

        assertEquals(2, viewModel.allHobbies.size)
        assertEquals("Hobby 1", viewModel.allHobbies[0])
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
    fun onSaveHobbiesCallsRepoAndSetsErrorOnFailure() = runTest {
        viewModel.hobbies = mutableSetOf("hobby1")
        coVerify { UserInformationRepositoryImpl.addOrUpdateUserInformation(any(), any()) }
        val result = viewModel.onSaveHobbies(context)

        assertFalse(result)
        assertEquals("Error updating hobbies", viewModel.hobbiesError?.message)
        coVerify { UserInformationRepositoryImpl.addOrUpdateUserInformation(any(), any()) }
    }

    @Test
    fun onSaveHobbiesWithSuccessClearsError() = runTest {
        viewModel.hobbiesError = AuthError("Test error")
        coEvery { UserInformationRepositoryImpl.addOrUpdateUserInformation(any(), any()) } returns RepositoryResult.SUCCESS

        val result = viewModel.onSaveHobbies(context)

        assertTrue(result)
        assertNull(viewModel.hobbiesError)
    }

    @Test
    fun onChangeUsernameWithInvalidInputSetsError() = runTest {
        viewModel.newUsername = "short"
        viewModel.usernameChangePassword = ""

        val result = viewModel.onChangeUsername(context)

        assertFalse(result)
        assertEquals("Fill all fields", viewModel.usernameError?.message)
    }

    @Test
    fun onChangeUsernameCallsReauthAndRepoOnSuccess() = runTest {
        viewModel.newUsername = "newuser"
        viewModel.usernameChangePassword = "Password123!"
        coEvery { UserInformationRepositoryImpl.isUsernameAvailable(any()) } returns true
        coEvery { UserInformationRepositoryImpl.addOrUpdateUserInformation(any(), any()) } returns RepositoryResult.SUCCESS

        val result = viewModel.onChangeUsername(context)

        assertTrue(result)
        assertNull(viewModel.usernameError)
        coVerify { UserInformationRepositoryImpl.isUsernameAvailable("newuser") }
        coVerify { UserInformationRepositoryImpl.addOrUpdateUserInformation(any(), any()) }
    }

    @Test
    fun onChangeUsernameWithTakenUsernameSetsError() = runTest {
        viewModel.newUsername = "taken"
        viewModel.usernameChangePassword = "Password123!"
        coEvery { UserInformationRepositoryImpl.isUsernameAvailable(any()) } returns false

        val result = viewModel.onChangeUsername(context)

        assertFalse(result)
        assertEquals("Username taken", viewModel.usernameError?.message)
    }

    @Test
    fun onChangePasswordWithInvalidFormSetsError() = runTest {
        viewModel.newPassword = "short"
        viewModel.repeatNewPassword = "different"

        val result = viewModel.onChangePassword(context)

        assertFalse(result)
        assertEquals("Invalid passwords", viewModel.passwordError?.message)
    }

    @Test
    fun onChangePasswordCallsReauthAndUpdateOnSuccess() = runTest {
        viewModel.currentPassword = "Current123!"
        viewModel.newPassword = "NewPassword123!"
        viewModel.repeatNewPassword = "NewPassword123!"

        val result = viewModel.onChangePassword(context)

        assertTrue(result)
        assertNull(viewModel.passwordError)
    }

    @Test
    fun onDeleteAccountWithEmptyPasswordSetsError() = runTest {
        viewModel.onDeleteAccount(context)

        assertEquals("Password required", viewModel.deleteAccountError?.message)
    }

    @Test
    fun onDeleteAccountCallsReauthAndDeleteOnSuccess() = runTest {
        viewModel.deletePassword = "Password123!"

        viewModel.onDeleteAccount(context)

        assertNull(viewModel.deleteAccountError)
    }

    @Test
    fun onRequestDataFetchesDataAndSetsJsonData() = runTest {
        viewModel.onRequestData()

        assertEquals("{}", viewModel.jsonData)
    }

    @Test
    fun resetJsonDataClearsJsonData() = runTest {
        viewModel.jsonData = "{}"

        viewModel.resetJsonData()

        assertNull(viewModel.jsonData)
    }

    @Test
    fun onUsernameChangePasswordChangeValidatesPassword() = runTest {
        viewModel.onUsernameChangePasswordChange("Password123!")

        assertTrue(viewModel.usernameChangePasswordIsValid)
    }

    @Test
    fun onCurrentPasswordChangeValidatesPassword() = runTest {
        viewModel.onCurrentPasswordChange("Current123!")

        assertTrue(viewModel.currentPasswordIsValid)
    }

    @Test
    fun onNewPasswordChangeValidatesPassword() = runTest {
        viewModel.onNewPasswordChange("NewPassword123!")

        assertTrue(viewModel.newPasswordIsValid)
    }

    @Test
    fun passwordChangeFormValidReturnsTrueForValidForm() = runTest {
        viewModel.currentPassword = "Current123!"
        viewModel.newPassword = "NewPassword123!"
        viewModel.repeatNewPassword = "NewPassword123!"

        assertTrue(viewModel.passwordChangeFormValid)
    }

    @Test
    fun passwordChangeFormValidReturnsFalseForInvalidForm() = runTest {
        viewModel.currentPassword = "short"
        viewModel.newPassword = "NewPassword123!"
        viewModel.repeatNewPassword = "Different"

        assertFalse(viewModel.passwordChangeFormValid)
    }

    @Test
    fun toggleUsernameChangePasswordVisibilityTogglesVisibility() = runTest {
        assertFalse(viewModel.isUsernameChangePasswordVisible)

        viewModel.toggleUsernameChangePasswordVisibility()

        assertTrue(viewModel.isUsernameChangePasswordVisible)
    }

    @Test
    fun toggleCurrentPasswordVisibilityTogglesVisibility() = runTest {
        assertFalse(viewModel.isCurrentPasswordVisible)

        viewModel.toggleCurrentPasswordVisibility()

        assertTrue(viewModel.isCurrentPasswordVisible)
    }

    @Test
    fun toggleNewPasswordVisibilityTogglesVisibility() = runTest {
        assertFalse(viewModel.isNewPasswordVisible)

        viewModel.toggleNewPasswordVisibility()

        assertTrue(viewModel.isNewPasswordVisible)
    }

    @Test
    fun toggleRepeatPasswordVisibilityTogglesVisibility() = runTest {
        assertFalse(viewModel.isRepeatPasswordVisible)

        viewModel.toggleRepeatPasswordVisibility()

        assertTrue(viewModel.isRepeatPasswordVisible)
    }

    @Test
    fun clearErrorClearsError() = runTest {
        viewModel.hobbiesError = AuthError("Test error")

        viewModel.clearError()

        assertNull(viewModel.hobbiesError)
    }
}