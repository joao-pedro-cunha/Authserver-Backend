package br.pucpr.authserver.students

import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

/** Filtros opcionais da consulta de estudantes: só entram na query os parâmetros informados. */
object StudentSpecifications {
    fun filter(name: String?, email: String?): Specification<Student> =
        Specification<Student> { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            if (!name.isNullOrBlank()) {
                predicates += cb.like(cb.lower(root.get<String>("name")), "%${name.trim().lowercase()}%")
            }
            if (!email.isNullOrBlank()) {
                predicates += cb.like(cb.lower(root.get<String>("email")), "%${email.trim().lowercase()}%")
            }

            cb.and(*predicates.toTypedArray())
        }
}
