package com.example.bookapp.data.repository

import com.example.bookapp.data.api.BookService
import com.example.bookapp.data.local.dao.BookDao
import com.example.bookapp.data.local.entity.BookEntity
import com.example.bookapp.data.model.BookItem
import com.example.bookapp.data.model.BookResponse
import com.example.bookapp.data.model.VolumeInfo
import com.example.bookapp.domain.model.Book
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class BookRepositoryTest {
  private lateinit var bookService: BookService
  private lateinit var bookDao: BookDao
  private lateinit var repository: BookRepository

  @Before
  fun setup() {
    bookService = mock()
    bookDao = mock()
    repository = BookRepository(bookService, bookDao)
  }

  @Test
  fun `searchBooks returns mapped books when API call is successful`() =
      runTest { // runTest: Special coroutine test scope for handling suspend functions
        /* Test Data Setup ("Given"):
           - Creates mock API response matching the structure expected from Google Books API
           - Corresponds to the data classes used in BookResponse.kt
        */
        val bookResponse =
            BookResponse(
                items =
                    listOf(
                        BookItem(
                            id = "test_id",
                            volumeInfo =
                                VolumeInfo(
                                    title = "Test Book",
                                    authors = listOf("Test Author"),
                                    description = "Test Description",
                                    publishedDate = "2024",
                                    pageCount = 100,
                                    categories = listOf("Fiction")))))

        whenever(bookService.searchBooks("test query"))
            .thenReturn(bookResponse) /* - Configures mock BookService to return our test data
            - When repository calls searchBooks("test query"), it returns our bookResponse
            - References the real BookService interface with the @GET("volumes") endpoint
         */

        /* When:
        -Calls actual repository method being tested
         -This triggers the mock BookService we configured
         */
        val result = repository.searchBooks("test query")

        /* Then:
        - Verifies the repository correctly mapped API response to domain model
        - Checks each field was properly converted */
        assertEquals(1, result.size)
        assertEquals("test_id", result[0].id)
        assertEquals("Test Book", result[0].title)
        assertEquals("Test Author", result[0].author)
        assertEquals("Test Description", result[0].description)
        assertEquals("2024", result[0].publishedDate)
        assertEquals(100, result[0].pageCount)
        assertEquals(listOf("Fiction"), result[0].categories)
      }

  @Test
  fun `searchBooks returns empty list when API call returns null items`() = runTest {
    val bookResponse = BookResponse(items = null)
    whenever(bookService.searchBooks("test query")).thenReturn(bookResponse)

    val result = repository.searchBooks("test query")

    assertEquals(0, result.size)
  }

  @Test
  fun `getSavedBooks returns mapped books from database`() = runTest {
    val bookEntity =
        BookEntity(
            id = "test_id",
            title = "Test Book",
            author = "Test Author",
            description = "Test Description",
            thumbnailUrl = "test_url",
            publishedDate = "2024",
            pageCount = 100,
            categories = "Fiction,Drama")
    whenever(bookDao.getAllBooks()).thenReturn(flowOf(listOf(bookEntity)))

    val result = repository.getSavedBooks().first()

    assertEquals(1, result.size)
    with(result[0]) {
      assertEquals("test_id", id)
      assertEquals("Test Book", title)
      assertEquals("Test Author", author)
      assertEquals(listOf("Fiction", "Drama"), categories)
    }
  }

  @Test
  fun `saveBook correctly stores book in database`() = runTest {
    val book =
        Book(
            id = "test_id",
            title = "Test Book",
            author = "Test Author",
            description = "Test Description",
            thumbnailUrl = "test_url",
            publishedDate = "2024",
            pageCount = 100,
            categories = listOf("Fiction", "Drama"))

    repository.saveBook(book)

    verify(bookDao)
        .insertBook(
            BookEntity(
                id = "test_id",
                title = "Test Book",
                author = "Test Author",
                description = "Test Description",
                thumbnailUrl = "test_url",
                publishedDate = "2024",
                pageCount = 100,
                categories = "Fiction,Drama"))
  }
}