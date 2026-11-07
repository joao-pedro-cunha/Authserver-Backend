package br.pucpr.authserver.enrollments

import br.pucpr.authserver.courses.Course
import br.pucpr.authserver.students.Student
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDate

/**
 * Tabela intermediária do many-to-many Student <-> Course. Tem atributos próprios
 * (data e status) e impede a mesma matrícula duas vezes.
 */
@Entity
@Table(
    name = "tbl_enrollments",
    uniqueConstraints = [UniqueConstraint(name = "uk_enrollment_student_course", columnNames = ["student_id", "course_id"])]
)
class Enrollment(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    val student: Student,

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    val course: Course,

    @Column(nullable = false)
    val enrollmentDate: LocalDate = LocalDate.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: EnrollmentStatus = EnrollmentStatus.ACTIVE
) {
    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Enrollment) return false
        return id != null && id == other.id
    }

    final override fun hashCode(): Int = javaClass.hashCode()

    override fun toString() = "Enrollment(id=$id)"
}
