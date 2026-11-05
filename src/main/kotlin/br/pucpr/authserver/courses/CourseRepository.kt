package br.pucpr.authserver.courses

import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CourseRepository : JpaRepository<Course, Long> {
    fun findByTitleContainingIgnoreCase(title: String, sort: Sort): List<Course>
}

@Repository
interface CategoryRepository : JpaRepository<Category, Long>