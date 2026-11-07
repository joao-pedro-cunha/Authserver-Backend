package br.pucpr.authserver.courses

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface CourseRepository : JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {
    fun existsByCategoryId(categoryId: Long): Boolean
}
