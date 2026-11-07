package br.pucpr.authserver.courses

import br.pucpr.authserver.categories.Category
import br.pucpr.authserver.categories.CategoryService
import br.pucpr.authserver.exceptions.BadRequestException
import br.pucpr.authserver.exceptions.NotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

class CourseServiceTest {
    private val repository = mockk<CourseRepository>()
    private val categoryService = mockk<CategoryService>()
    private val service = CourseService(repository, categoryService)

    private val backend = Category(id = 1, name = "Backend")
    private val frontend = Category(id = 2, name = "Frontend")

    @BeforeEach
    fun setup() = clearAllMocks()

    @Test
    fun `insert links the course to its category`() {
        every { categoryService.findById(1L) } returns backend
        every { repository.save(any<Course>()) } answers { firstArg() }

        val result = service.insert("  Kotlin  ", 40, 1L)

        result.title shouldBe "Kotlin"
        result.category shouldBe backend
    }

    @Test
    fun `insert fails when the category does not exist`() {
        every { categoryService.findById(99L) } throws NotFoundException("Categoria 99 não encontrada.")

        shouldThrow<NotFoundException> { service.insert("Kotlin", 40, 99L) }

        verify(exactly = 0) { repository.save(any<Course>()) }
    }

    @Test
    fun `changeCategory moves the course to another category`() {
        val course = Course(id = 5, title = "Kotlin", workload = 40, category = backend)
        every { repository.findById(5L) } returns Optional.of(course)
        every { categoryService.findById(2L) } returns frontend
        every { repository.save(course) } returns course

        val result = service.changeCategory(5L, 2L)

        result.category shouldBe frontend
    }

    @Test
    fun `search rejects minWorkload greater than maxWorkload`() {
        shouldThrow<BadRequestException> { service.search(null, null, 50, 10, null, null) }
    }

    @Test
    fun `search rejects an unknown sort field`() {
        shouldThrow<BadRequestException> { service.search(null, null, null, null, "enrollments", null) }
    }
}
