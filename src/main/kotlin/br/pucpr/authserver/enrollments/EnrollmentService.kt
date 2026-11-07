package br.pucpr.authserver.enrollments

import br.pucpr.authserver.common.Sorting
import br.pucpr.authserver.courses.CourseService
import br.pucpr.authserver.exceptions.ConflictException
import br.pucpr.authserver.exceptions.NotFoundException
import br.pucpr.authserver.security.CurrentUser
import br.pucpr.authserver.students.StudentService
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EnrollmentService(
    private val repository: EnrollmentRepository,
    private val studentService: StudentService,
    private val courseService: CourseService
) {
    /** Associa um estudante a um curso. */
    @Transactional
    fun enroll(studentId: Long, courseId: Long): Enrollment {
        val student = studentService.findById(studentId)
        val course = courseService.findById(courseId)
        if (repository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw ConflictException(
                "O estudante $studentId já está matriculado no curso $courseId. " +
                    "Para reativar uma matrícula existente, altere o status dela."
            )
        }
        return repository.save(Enrollment(student = student, course = course))
            .also { log.info("Estudante {} matriculado no curso {} (matrícula {})", studentId, courseId, it.id) }
    }

    @Transactional(readOnly = true)
    fun search(
        studentId: Long?,
        courseId: Long?,
        status: EnrollmentStatus?,
        sortBy: String?,
        sortDir: String?
    ): List<Enrollment> {
        val sort = Sorting.resolve(sortBy, sortDir, SORTABLE_FIELDS, default = "enrollmentDate")
        log.debug("Consultando matrículas (studentId={}, courseId={}, status={}, sort={})", studentId, courseId, status, sort)
        return repository.findAll(EnrollmentSpecifications.filter(studentId, courseId, status), sort)
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): Enrollment =
        repository.findByIdOrNull(id)
            ?: throw NotFoundException("Matrícula $id não encontrada.")

    @Transactional
    fun updateStatus(id: Long, status: EnrollmentStatus): Enrollment {
        val enrollment = findById(id)
        val previous = enrollment.status
        enrollment.status = status
        return repository.save(enrollment)
            .also { log.info("Matrícula {} mudou de {} para {} por {}", id, previous, status, CurrentUser.label()) }
    }

    /** Desfaz a associação entre estudante e curso. */
    @Transactional
    fun unenroll(id: Long) {
        val enrollment = findById(id)
        repository.delete(enrollment)
        log.warn("Matrícula {} removida por {}", id, CurrentUser.label())
    }

    companion object {
        private val log = LoggerFactory.getLogger(EnrollmentService::class.java)
        private val SORTABLE_FIELDS = mapOf(
            "id" to "id",
            "enrollmentDate" to "enrollmentDate",
            "status" to "status",
            "student" to "student.name",
            "course" to "course.title"
        )
    }
}
