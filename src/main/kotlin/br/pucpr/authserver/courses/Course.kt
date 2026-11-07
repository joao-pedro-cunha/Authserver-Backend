package br.pucpr.authserver.courses

import br.pucpr.authserver.categories.Category
import br.pucpr.authserver.enrollments.Enrollment
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

/**
 * Lado "muitos" de Category -> Course (one-to-many) e um dos lados do many-to-many
 * Student <-> Course, que é feito pela entidade intermediária [Enrollment].
 */
@Entity
@Table(name = "tbl_courses")
class Course(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false)
    var workload: Int,

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category,

    @OneToMany(mappedBy = "course", cascade = [CascadeType.ALL], orphanRemoval = true)
    val enrollments: MutableList<Enrollment> = mutableListOf()
) {
    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Course) return false
        return id != null && id == other.id
    }

    final override fun hashCode(): Int = javaClass.hashCode()

    override fun toString() = "Course(id=$id, title=$title)"
}
