package br.pucpr.authserver.courses

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "tbl_students")
data class Student(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    var name: String,
    @Column(nullable = false, unique = true)
    var email: String,
    @JsonIgnore
    @OneToMany(mappedBy = "student", cascade = [CascadeType.ALL], orphanRemoval = true)
    var enrollments: MutableList<Enrollment> = mutableListOf()
)

@Entity
@Table(name = "tbl_enrollments")
data class Enrollment(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    var student: Student,
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    var course: Course,
    @Column(nullable = false)
    var enrollmentDate: LocalDate = LocalDate.now(),
    @Column(nullable = false)
    var status: String = "ACTIVE"
)