package br.pucpr.authserver.enrollments.responses

import br.pucpr.authserver.enrollments.Enrollment
import br.pucpr.authserver.enrollments.EnrollmentStatus
import java.time.LocalDate

data class EnrollmentResponse(
    val id: Long,
    val studentId: Long,
    val studentName: String,
    val courseId: Long,
    val courseTitle: String,
    val enrollmentDate: LocalDate,
    val status: EnrollmentStatus
) {
    constructor(enrollment: Enrollment) : this(
        id = enrollment.id!!,
        studentId = enrollment.student.id!!,
        studentName = enrollment.student.name,
        courseId = enrollment.course.id!!,
        courseTitle = enrollment.course.title,
        enrollmentDate = enrollment.enrollmentDate,
        status = enrollment.status
    )
}
