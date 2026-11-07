package br.pucpr.authserver.enrollments

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface EnrollmentRepository : JpaRepository<Enrollment, Long>, JpaSpecificationExecutor<Enrollment> {
    fun existsByStudentIdAndCourseId(studentId: Long, courseId: Long): Boolean
}
