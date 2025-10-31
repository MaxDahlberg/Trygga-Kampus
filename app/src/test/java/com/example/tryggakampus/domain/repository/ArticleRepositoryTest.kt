package com.example.tryggakampus.domain.repository

import com.example.tryggakampus.domain.model.ArticleModel
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.*
import io.mockk.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test


class ArticleRepositoryTest {

    private val mockFirestore: FirebaseFirestore = mockk()
    private val mockCollection: CollectionReference = mockk()
    private val mockQuerySnapshot: QuerySnapshot = mockk()
    private val mockDocument: DocumentSnapshot = mockk()
    private val mockDocumentReference: DocumentReference = mockk()

    private lateinit var repository: ArticleRepository

    @Before
    fun setup() {
        mockkStatic(Firebase::class)
        every { Firebase.firestore } returns mockFirestore
        every { mockFirestore.collection("articles") } returns mockCollection

        every { mockQuerySnapshot.documents } returns listOf(mockDocument)
        every { mockDocument.toObject(ArticleModel::class.java) } returns ArticleModel(title = "Test Title")
        every { mockDocument.id } returns "test-id"

        repository = ArticleRepositoryImpl
    }

    @After
    fun teardown() {
        unmockkAll()
        unmockkStatic(Firebase::class)
    }

    @Test
    fun `getAllArticles with SERVER source succeeds and maps models`() = runTest {
        val mockTask = mockk<Task<QuerySnapshot>>()
        coEvery { mockCollection.get(Source.SERVER) } returns mockTask
        coEvery { mockTask.await() } returns mockQuerySnapshot

        val (result, articles) = repository.getAllArticles(Source.SERVER)

        assertEquals(ArticleRepository.RepositoryResult.SUCCESS, result)
        assertEquals(1, articles.size)
        val article = articles[0]
        assertEquals("test-id", article.id)
        coVerify { mockCollection.get(Source.SERVER) }
    }

    @Test
    fun `getAllArticles with SERVER fail falls back to CACHE and succeeds`() = runTest {
        val serverTask = mockk<Task<QuerySnapshot>>()
        val cacheTask = mockk<Task<QuerySnapshot>>()
        coEvery { mockCollection.get(Source.SERVER) } returns serverTask
        coEvery { serverTask.await() } throws FirebaseFirestoreException("Network error", FirebaseFirestoreException.Code.UNAVAILABLE)
        coEvery { mockCollection.get(Source.CACHE) } returns cacheTask
        coEvery { cacheTask.await() } returns mockQuerySnapshot

        val (result, articles) = repository.getAllArticles(Source.SERVER)

        assertEquals(ArticleRepository.RepositoryResult.SUCCESS, result)
        assertEquals(1, articles.size)
        coVerify { mockCollection.get(Source.SERVER) }
        coVerify { mockCollection.get(Source.CACHE) }
    }

    @Test
    fun `getAllArticles with general error returns empty list`() = runTest {
        val serverTask = mockk<Task<QuerySnapshot>>()
        coEvery { mockCollection.get(Source.SERVER) } returns serverTask
        coEvery { serverTask.await() } throws Exception("Unknown error")

        val (result, articles) = repository.getAllArticles(Source.SERVER)

        assertEquals(ArticleRepository.RepositoryResult.ERROR_DATABASE, result)
        assertTrue(articles.isEmpty())
    }

    @Test
    fun `addArticle succeeds and returns model with ID`() = runTest {
        val mockAddTask = mockk<Task<DocumentReference>>()
        val mockGetTask = mockk<Task<DocumentSnapshot>>()
        every { mockCollection.add(any()) } returns mockAddTask
        coEvery { mockAddTask.await() } returns mockDocumentReference
        every { mockDocumentReference.id } returns "new-id"
        coEvery { mockDocumentReference.get() } returns mockGetTask
        coEvery { mockGetTask.await() } returns mockDocument

        val testArticle = ArticleModel(title = "New Article")

        val (result, addedArticle) = repository.addArticle(testArticle)

        assertEquals(ArticleRepository.RepositoryResult.SUCCESS, result)
        assertEquals("new-id", addedArticle?.id)
        assertEquals("New Article", addedArticle?.title)
        coVerify { mockCollection.add(testArticle) }
    }

    @Test
    fun `addArticle network error returns null article`() = runTest {
        val mockAddTask = mockk<Task<DocumentReference>>()
        every { mockCollection.add(any()) } returns mockAddTask
        coEvery { mockAddTask.await() } throws FirebaseFirestoreException("Network issue", FirebaseFirestoreException.Code.UNAVAILABLE)

        val testArticle = ArticleModel(title = "Failing Article")

        val (result, addedArticle) = repository.addArticle(testArticle)

        assertEquals(ArticleRepository.RepositoryResult.ERROR_NETWORK, result)
        assertEquals(null, addedArticle)
    }

    @Test
    fun `addArticle unknown error returns null article`() = runTest {
        val mockAddTask = mockk<Task<DocumentReference>>()
        every { mockCollection.add(any()) } returns mockAddTask
        coEvery { mockAddTask.await() } throws Exception("Unexpected")

        val (result, _) = repository.addArticle(ArticleModel(title = ""))

        assertEquals(ArticleRepository.RepositoryResult.ERROR_UNKNOWN, result)
    }

    @Test
    fun `deleteArticle succeeds`() = runTest {
        val mockDeleteTask = mockk<Task<Void>>()
        every { mockFirestore.collection("articles").document("test-id") } returns mockDocumentReference
        every { mockDocumentReference.delete() } returns mockDeleteTask
        coEvery { mockDeleteTask.await() } returns mockk()

        val result = repository.deleteArticle("test-id")

        assertEquals(ArticleRepository.RepositoryResult.SUCCESS, result)
        coVerify { mockDocumentReference.delete() }
    }

    @Test
    fun `deleteArticle network error returns ERROR_NETWORK`() = runTest {
        val mockDeleteTask = mockk<Task<Void>>()
        every { mockFirestore.collection("articles").document("test-id") } returns mockDocumentReference
        every { mockDocumentReference.delete() } returns mockDeleteTask
        coEvery { mockDeleteTask.await() } throws FirebaseFirestoreException("Delete failed", FirebaseFirestoreException.Code.PERMISSION_DENIED)

        val result = repository.deleteArticle("test-id")

        assertEquals(ArticleRepository.RepositoryResult.ERROR_NETWORK, result)
    }

    @Test
    fun `deleteArticle unknown error returns ERROR_UNKNOWN`() = runTest {
        val mockDeleteTask = mockk<Task<Void>>()
        every { mockFirestore.collection("articles").document("test-id") } returns mockDocumentReference
        every { mockDocumentReference.delete() } returns mockDeleteTask
        coEvery { mockDeleteTask.await() } throws Exception("Delete crash")

        val result = repository.deleteArticle("test-id")

        assertEquals(ArticleRepository.RepositoryResult.ERROR_UNKNOWN, result)
    }

    @Test
    fun `fetchAll with CACHE source returns snapshot`() = runTest {
        val mockTask = mockk<Task<QuerySnapshot>>()
        coEvery { mockCollection.get(Source.CACHE) } returns mockTask
        coEvery { mockTask.await() } returns mockQuerySnapshot

        val snapshot = repository.fetchAll(Source.CACHE)

        assertEquals(mockQuerySnapshot, snapshot)
        coVerify { mockCollection.get(Source.CACHE) }
    }

    @Test
    fun `fetchAll with SERVER fail falls back to CACHE`() = runTest {
        val serverTask = mockk<Task<QuerySnapshot>>()
        val cacheTask = mockk<Task<QuerySnapshot>>()
        coEvery { mockCollection.get(Source.SERVER) } returns serverTask
        coEvery { serverTask.await() } throws FirebaseFirestoreException("Offline", FirebaseFirestoreException.Code.UNAVAILABLE)
        coEvery { mockCollection.get(Source.CACHE) } returns cacheTask
        coEvery { cacheTask.await() } returns mockQuerySnapshot

        val snapshot = repository.fetchAll(Source.SERVER)

        assertEquals(mockQuerySnapshot, snapshot)
    }

    @Test
    fun `fetchAll general error returns null`() = runTest {
        val mockTask = mockk<Task<QuerySnapshot>>()
        coEvery { mockCollection.get(Source.SERVER) } returns mockTask
        coEvery { mockTask.await() } throws Exception("Total fail")

        val snapshot = repository.fetchAll(Source.SERVER)

        assertEquals(null, snapshot)
    }
}