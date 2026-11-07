package br.pucpr.authserver.courses

import br.pucpr.authserver.categories.CategoryService
import br.pucpr.authserver.common.Sorting
import br.pucpr.authserver.exceptions.BadRequestException
import br.pucpr.authserver.exceptions.NotFoundException
import br.pucpr.authserver.security.CurrentUser
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CourseService(
    private val repository: CourseRepository,
    private val categoryService: CategoryService
) {
    @Transactional
    fun insert(title: String, workload: Int, categoryId: Long): Course {
        val category = categoryService.findById(categoryId)
        return repository.save(Course(title = title.trim(), workload = workload, category = category))
            .also { log.info("Curso {} criado na categoria {}", it.id, categoryId) }
    }

    @Transactional(readOnly = true)
    fun search(
        title: String?,
        categoryId: Long?,
        minWorkload: Int?,
        maxWorkload: Int?,
        sortBy: String?,
        sortDir: String?
    ): List<Course> {
        if (minWorkload != null && maxWorkload != null && minWorkload > maxWorkload) {
            throw BadRequestException("minWorkload ($minWorkload) não pode ser maior que maxWorkload ($maxWorkload).")
        }
        val sort = Sorting.resolve(sortBy, sortDir, SORTABLE_FIELDS, default = "title")
        log.debug(
            "Consultando cursos (title={}, categoryId={}, minWorkload={}, maxWorkload={}, sort={})",
            title, categoryId, minWorkload, maxWorkload, sort
        )
        return repository.findAll(CourseSpecifications.filter(title, categoryId, minWorkload, maxWorkload), sort)
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): Course =
        repository.findByIdOrNull(id)
            ?: throw NotFoundException("Curso $id não encontrado.")

    @Transactional
    fun update(id: Long, title: String, workload: Int, categoryId: Long): Course {
        val course = findById(id)
        course.title = title.trim()
        course.workload = workload
        course.category = categoryService.findById(categoryId)
        return repository.save(course)
            .also { log.info("Curso {} atualizado por {}", id, CurrentUser.label()) }
    }

    /** Associação Course -> Category (move o curso para outra categoria). */
    @Transactional
    fun changeCategory(id: Long, categoryId: Long): Course {
        val course = findById(id)
        course.category = categoryService.findById(categoryId)
        return repository.save(course)
            .also { log.info("Curso {} movido para a categoria {} por {}", id, categoryId, CurrentUser.label()) }
    }

    @Transactional
    fun delete(id: Long) {
        val course = findById(id)
        val enrollments = course.enrollments.size
        repository.delete(course)
        log.warn("Curso {} removido por {} (matrículas removidas junto: {})", id, CurrentUser.label(), enrollments)
    }

    companion object {
        private val log = LoggerFactory.getLogger(CourseService::class.java)
        private val SORTABLE_FIELDS = mapOf(
            "id" to "id",
            "title" to "title",
            "workload" to "workload",
            "category" to "category.name"
        )
    }
}
