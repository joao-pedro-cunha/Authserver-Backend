package br.pucpr.authserver.courses

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
@Table(name = "tbl_categories")
data class Category(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, unique = true)
    var name: String,
    @JsonIgnore
    @OneToMany(mappedBy = "category", cascade = [CascadeType.ALL])
    val courses: MutableList<Course> = mutableListOf()
)

@Entity
@Table(name = "tbl_courses")
data class Course(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    var title: String,
    @Column(nullable = false)
    var workload: Int,
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category,
    @JsonIgnore
    @OneToMany(mappedBy = "course", cascade = [CascadeType.ALL], orphanRemoval = true)
    var enrollments: MutableList<Enrollment> = mutableListOf()
)