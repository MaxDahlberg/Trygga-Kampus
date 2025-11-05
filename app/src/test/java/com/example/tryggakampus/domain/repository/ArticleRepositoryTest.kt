package com.example.tryggakampus.domain.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ArticleRepositoryTest {

    @Test
    fun `repository result enum contains expected values`() {
        assertEquals("SUCCESS", ArticleRepository.RepositoryResult.SUCCESS.name)
        assertEquals("ERROR_NETWORK", ArticleRepository.RepositoryResult.ERROR_NETWORK.name)
        assertEquals("ERROR_DATABASE", ArticleRepository.RepositoryResult.ERROR_DATABASE.name)
        assertEquals("ERROR_UNKNOWN", ArticleRepository.RepositoryResult.ERROR_UNKNOWN.name)
    }

    @Test
    fun `repository implementation object is accessible`() {
        // Ensure the singleton can be referenced in unit tests without initializing Firebase
        assertNotNull(ArticleRepositoryImpl)
    }
}