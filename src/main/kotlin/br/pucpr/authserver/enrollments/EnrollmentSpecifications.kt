package br.pucpr.authserver.enrollments

import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

/** Filtros opcionais da consulta de matrículas: só entram na query os parâmetros informados. */
object EnrollmentSpecifications {
    fun filter(
        studentId: Long?,
        courseId: Long?,
        status: EnrollmentStatus?
    ): Specification<Enrollment> = Specification<Enrollment> { root, _, cb ->
        val predicates = mutableListOf<Predicate>()

        if (studentId != null) {
            predicates += cb.equal(root.get<Any>("student").get<Long>("id"), studentId)
        }
        if (courseId != null) {
            predicates += cb.equal(root.get<Any>("course").get<Long>("id"), courseId)
        }
        if (status != null) {
            predicates += cb.equal(root.get<EnrollmentStatus>("status"), status)
        }

        cb.and(*predicates.toTypedArray())
    }
}
