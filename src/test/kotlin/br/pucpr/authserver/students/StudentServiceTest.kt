package br.pucpr.authserver.students

import br.pucpr.authserver.exceptions.BadRequestException
import br.pucpr.authserver.exceptions.ConflictException
import br.pucpr.authserver.exceptions.NotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification

class StudentServiceTest {
    private val repository = mockk<StudentRepository>()
    private val service = StudentService(repository)

    @BeforeEach
    fun setup() = clearAllMocks()

    @Test
    fun `insert normalizes the email and saves`() {
        every { repository.findByEmail("ana@email.com") } returns null
        every { repository.save(any<Student>()) } answers { firstArg() }

        val result = service.insert("  Ana  ", "  ANA@Email.com ")

        result.name shouldBe "Ana"
        result.email shouldBe "ana@email.com"
        verify(exactly = 1) { repository.save(any<Student>()) }
    }

    @Test
    fun `insert fails with conflict when the email already exists`() {
        every { repository.findByEmail("ana@email.com") } returns Student(id = 9, name = "Outra Ana", email = "ana@email.com")

        shouldThrow<ConflictException> { service.insert("Ana", "ana@email.com") }

        verify(exactly = 0) { repository.save(any<Student>()) }
    }

    @Test
    fun `findById fails with not found`() {
        every { repository.findById(42L) } returns java.util.Optional.empty()

        shouldThrow<NotFoundException> { service.findById(42L) }
    }

    @Test
    fun `update fails with conflict when the email belongs to another student`() {
        every { repository.findById(1L) } returns java.util.Optional.of(Student(id = 1, name = "Ana", email = "ana@email.com"))
        every { repository.findByEmail("bia@email.com") } returns Student(id = 2, name = "Bia", email = "bia@email.com")

        shouldThrow<ConflictException> { service.update(1L, "Ana", "bia@email.com") }
    }

    @Test
    fun `update accepts the student keeping their own email`() {
        val student = Student(id = 1, name = "Ana", email = "ana@email.com")
        every { repository.findById(1L) } returns java.util.Optional.of(student)
        every { repository.findByEmail("ana@email.com") } returns student
        every { repository.save(student) } returns student

        val result = service.update(1L, "Ana Maria", "ana@email.com")

        result.name shouldBe "Ana Maria"
    }

    @Test
    fun `search rejects a sort field outside the whitelist`() {
        shouldThrow<BadRequestException> { service.search(null, null, "password", null) }
    }

    @Test
    fun `search applies the requested sort`() {
        val sort = io.mockk.slot<Sort>()
        every { repository.findAll(any<Specification<Student>>(), capture(sort)) } returns emptyList()

        service.search("ana", null, "email", "DESC")

        sort.captured shouldBe Sort.by(Sort.Direction.DESC, "email")
    }

    @Test
    fun `delete removes the student`() {
        val student = Student(id = 1, name = "Ana", email = "ana@email.com")
        every { repository.findById(1L) } returns java.util.Optional.of(student)
        every { repository.delete(student) } just Runs

        service.delete(1L)

        verify(exactly = 1) { repository.delete(student) }
    }
}
