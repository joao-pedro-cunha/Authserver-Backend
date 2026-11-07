package br.pucpr.authserver.categories

import br.pucpr.authserver.courses.Course
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

/** Lado "um" da relação one-to-many Category -> Course. */
@Entity
@Table(name = "tbl_categories")
class Category(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    var name: String,

    // Sem cascade: apagar uma categoria nunca deve apagar cursos silenciosamente.
    @OneToMany(mappedBy = "category")
    val courses: MutableList<Course> = mutableListOf()
) {
    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Category) return false
        return id != null && id == other.id
    }

    final override fun hashCode(): Int = javaClass.hashCode()

    override fun toString() = "Category(id=$id, name=$name)"
}
