package br.pucpr.authserver.students

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface StudentRepository : JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {
    fun findByEmail(email: String): Student?
}
