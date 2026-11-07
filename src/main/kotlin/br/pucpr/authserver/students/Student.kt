package br.pucpr.authserver.students

import br.pucpr.authserver.enrollments.Enrollment
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

/** Um dos lados do many-to-many Student <-> Course, feito pela entidade intermediária [Enrollment]. */
@Entity
@Table(name = "tbl_students")
class Student(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var email: String,

    // Apagar o estudante remove também as matrículas dele (são só registros de ligação).
    @OneToMany(mappedBy = "student", cascade = [CascadeType.ALL], orphanRemoval = true)
    val enrollments: MutableList<Enrollment> = mutableListOf()
) {
    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Student) return false
        return id != null && id == other.id
    }

    final override fun hashCode(): Int = javaClass.hashCode()

    override fun toString() = "Student(id=$id)"
}
