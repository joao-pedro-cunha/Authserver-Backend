package br.pucpr.authserver.enrollments

import br.pucpr.authserver.categories.Category
import br.pucpr.authserver.courses.Course
import br.pucpr.authserver.courses.CourseService
import br.pucpr.authserver.exceptions.ConflictException
import br.pucpr.authserver.exceptions.NotFoundException
import br.pucpr.authserver.students.Student
import br.pucpr.authserver.students.StudentService
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
import java.util.Optional

class EnrollmentServiceTest {
    private val repository = mockk<EnrollmentRepository>()
    private val studentService = mockk<StudentService>()
    private val courseService = mockk<CourseService>()
    private val service = EnrollmentService(repository, studentService, courseService)

    private val student = Student(id = 1, name = "Ana", email = "ana@email.com")
    private val course = Course(id = 2, title = "Kotlin", workload = 40, category = Category(id = 1, name = "Backend"))

    @BeforeEach
    fun setup() = clearAllMocks()

    @Test
    fun `enroll creates an ACTIVE enrollment`() {
        every { studentService.findById(1L) } returns student
        every { courseService.findById(2L) } returns course
        every { repository.existsByStudentIdAndCourseId(1L, 2L) } returns false
        every { repository.save(any<Enrollment>()) } answers { firstArg() }

        val result = service.enroll(1L, 2L)

        result.student shouldBe student
        result.course shouldBe course
        result.status shouldBe EnrollmentStatus.ACTIVE
    }

    @Test
    fun `enroll fails with conflict when the student is already enrolled`() {
        every { studentService.findById(1L) } returns student
        every { courseService.findById(2L) } returns course
        every { repository.existsByStudentIdAndCourseId(1L, 2L) } returns true

        shouldThrow<ConflictException> { service.enroll(1L, 2L) }

        verify(exactly = 0) { repository.save(any<Enrollment>()) }
    }

    @Test
    fun `enroll fails when the student does not exist`() {
        every { studentService.findById(1L) } throws NotFoundException("Estudante 1 não encontrado.")

        shouldThrow<NotFoundException> { service.enroll(1L, 2L) }
    }

    @Test
    fun `updateStatus changes the status`() {
        val enrollment = Enrollment(id = 10, student = student, course = course)
        every { repository.findById(10L) } returns Optional.of(enrollment)
        every { repository.save(enrollment) } returns enrollment

        val result = service.updateStatus(10L, EnrollmentStatus.COMPLETED)

        result.status shouldBe EnrollmentStatus.COMPLETED
    }

    @Test
    fun `unenroll removes the enrollment`() {
        val enrollment = Enrollment(id = 10, student = student, course = course)
        every { repository.findById(10L) } returns Optional.of(enrollment)
        every { repository.delete(enrollment) } just Runs

        service.unenroll(10L)

        verify(exactly = 1) { repository.delete(enrollment) }
    }

    @Test
    fun `unenroll fails with not found for an unknown enrollment`() {
        every { repository.findById(10L) } returns Optional.empty()

        shouldThrow<NotFoundException> { service.unenroll(10L) }
    }
}
