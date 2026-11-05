package br.pucpr.authserver.courses

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/enrollments")
class EnrollmentController(
    private val studentRepository: StudentRepository,
    private val courseRepository: CourseRepository,
    private val enrollmentRepository: EnrollmentRepository
) {
    @PostMapping
    fun enroll(@RequestBody request: EnrollmentRequest): ResponseEntity<Enrollment> {
        val student = studentRepository.findById(request.studentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Estudante não encontrado.") }

        val course = courseRepository.findById(request.courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado.") }

        val enrollment = Enrollment(student = student, course = course)
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentRepository.save(enrollment))
    }

    @DeleteMapping("/{id}")
    fun unenroll(@PathVariable id: Long): ResponseEntity<Void> {
        if (!enrollmentRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Matrícula não encontrada.")
        }
        enrollmentRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}

data class EnrollmentRequest(val studentId: Long, val courseId: Long)