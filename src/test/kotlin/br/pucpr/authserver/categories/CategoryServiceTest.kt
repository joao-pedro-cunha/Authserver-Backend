package br.pucpr.authserver.categories

import br.pucpr.authserver.courses.CourseRepository
import br.pucpr.authserver.exceptions.ConflictException
import br.pucpr.authserver.exceptions.NotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

class CategoryServiceTest {
    private val repository = mockk<CategoryRepository>()
    private val courseRepository = mockk<CourseRepository>()
    private val service = CategoryService(repository, courseRepository)

    @BeforeEach
    fun setup() = clearAllMocks()

    @Test
    fun `insert fails with conflict when the name already exists`() {
        every { repository.existsByNameIgnoreCase("Backend") } returns true

        shouldThrow<ConflictException> { service.insert("Backend") }

        verify(exactly = 0) { repository.save(any<Category>()) }
    }

    @Test
    fun `delete is refused while the category still has courses`() {
        every { repository.findById(1L) } returns Optional.of(Category(id = 1, name = "Backend"))
        every { courseRepository.existsByCategoryId(1L) } returns true

        shouldThrow<ConflictException> { service.delete(1L) }

        verify(exactly = 0) { repository.delete(any<Category>()) }
    }

    @Test
    fun `delete removes an empty category`() {
        val category = Category(id = 1, name = "Backend")
        every { repository.findById(1L) } returns Optional.of(category)
        every { courseRepository.existsByCategoryId(1L) } returns false
        every { repository.delete(category) } just Runs

        service.delete(1L)

        verify(exactly = 1) { repository.delete(category) }
    }

    @Test
    fun `delete fails with not found for an unknown category`() {
        every { repository.findById(7L) } returns Optional.empty()

        shouldThrow<NotFoundException> { service.delete(7L) }
    }
}
