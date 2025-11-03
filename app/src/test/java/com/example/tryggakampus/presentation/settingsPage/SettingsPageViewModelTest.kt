package com.example.tryggakampus.presentation.settingsPage

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.*

class SettingsPageViewModelTest {

    private lateinit var viewModel: SettingsPageViewModel

    @Before
    fun setup() {
        viewModel = SettingsPageViewModel()
    }

    @Test
    fun `init sets currentLanguage to system default`() {
        val expectedLanguage = Locale.getDefault().language

        val vm = SettingsPageViewModel()

        assertEquals(expectedLanguage, vm.currentLanguage)
    }

    @Test
    fun `setTabIndex updates someTabIndex`() {
        val expectedTab = ArticleTabs.TAB_TWO

        viewModel.setTabIndex(expectedTab)

        assertEquals(expectedTab, viewModel.someTabIndex)
    }

    @Test
    fun `setLanguage updates currentLanguage`() {
        val expectedLanguage = "fr"

        viewModel.setLanguage(expectedLanguage)

        assertEquals(expectedLanguage, viewModel.currentLanguage)
    }

    @Test
    fun `setTabIndex changes from default TAB_ONE to TAB_THREE`() {

        viewModel.setTabIndex(ArticleTabs.TAB_THREE)

        assertEquals(ArticleTabs.TAB_THREE, viewModel.someTabIndex)
    }
}