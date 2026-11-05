package br.pucpr.authserver.courses

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class CourseService(
    private val courseRepository: CourseRepository,
    private val categoryRepository: CategoryRepository
) {
    private val log = LoggerFactory.getLogger(CourseService::class.java)

    fun create(title: String, workload: Int, categoryId: Long): Course {
        log.info("Criando novo curso: '$title' vinculado à categoria ID: $categoryId")
        val category = categoryRepository.findById(categoryId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria com ID $categoryId não encontrada.") }
        return courseRepository.save(Course(title = title, workload = workload, category = category))
    }

    fun listAll(titleQuery: String?, sortBy: String = "title"): List<Course> {
        log.info("Listando cursos. Filtro de título: '$titleQuery', Ordenado por: $sortBy")
        val sort = Sort.by(sortBy)
        return if (titleQuery.isNullOrBlank()) {
            courseRepository.findAll(sort)
        } else {
            courseRepository.findByTitleContainingIgnoreCase(titleQuery, sort)
        }
    }

    fun findById(id: Long): Course {
        log.info("Buscando curso ID: $id")
        return courseRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Curso com ID $id não encontrado.") }
    }

    fun update(id: Long, title: String, workload: Int): Course {
        log.info("Atualizando curso ID: $id")
        val course = findById(id)
        course.title = title
        course.workload = workload
        return courseRepository.save(course)
    }

    fun delete(id: Long) {
        log.warn("Removendo curso ID: $id")
        val course = findById(id)
        courseRepository.delete(course)
    }
}