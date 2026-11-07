package br.pucpr.authserver.courses

import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

/** Filtros opcionais da consulta de cursos: só entram na query os parâmetros informados. */
object CourseSpecifications {
    fun filter(
        title: String?,
        categoryId: Long?,
        minWorkload: Int?,
        maxWorkload: Int?
    ): Specification<Course> = Specification<Course> { root, _, cb ->
        val predicates = mutableListOf<Predicate>()

        if (!title.isNullOrBlank()) {
            predicates += cb.like(cb.lower(root.get<String>("title")), "%${title.trim().lowercase()}%")
        }
        if (categoryId != null) {
            predicates += cb.equal(root.get<Any>("category").get<Long>("id"), categoryId)
        }
        if (minWorkload != null) {
            predicates += cb.greaterThanOrEqualTo(root.get<Int>("workload"), minWorkload)
        }
        if (maxWorkload != null) {
            predicates += cb.lessThanOrEqualTo(root.get<Int>("workload"), maxWorkload)
        }

        cb.and(*predicates.toTypedArray())
    }
}
