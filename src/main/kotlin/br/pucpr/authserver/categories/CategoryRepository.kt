package br.pucpr.authserver.categories

import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<Category, Long> {
    fun existsByNameIgnoreCase(name: String): Boolean
    fun findByNameIgnoreCase(name: String): Category?
}
