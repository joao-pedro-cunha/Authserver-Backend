package br.pucpr.authserver.categories

import br.pucpr.authserver.common.Sorting
import br.pucpr.authserver.courses.CourseRepository
import br.pucpr.authserver.exceptions.ConflictException
import br.pucpr.authserver.exceptions.NotFoundException
import br.pucpr.authserver.security.CurrentUser
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryService(
    private val repository: CategoryRepository,
    private val courseRepository: CourseRepository
) {
    @Transactional
    fun insert(name: String): Category {
        val cleanName = name.trim()
        if (repository.existsByNameIgnoreCase(cleanName)) {
            throw ConflictException("Já existe uma categoria chamada '$cleanName'.")
        }
        return repository.save(Category(name = cleanName))
            .also { log.info("Categoria {} criada", it.id) }
    }

    @Transactional(readOnly = true)
    fun findAll(sortBy: String?, sortDir: String?): List<Category> {
        val sort = Sorting.resolve(sortBy, sortDir, SORTABLE_FIELDS, default = "name")
        log.debug("Listando categorias (sort={})", sort)
        return repository.findAll(sort)
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): Category =
        repository.findByIdOrNull(id)
            ?: throw NotFoundException("Categoria $id não encontrada.")

    @Transactional
    fun update(id: Long, name: String): Category {
        val category = findById(id)
        val cleanName = name.trim()
        val sameName = repository.findByNameIgnoreCase(cleanName)
        if (sameName != null && sameName.id != id) {
            throw ConflictException("Já existe uma categoria chamada '$cleanName'.")
        }
        category.name = cleanName
        return repository.save(category)
            .also { log.info("Categoria {} atualizada por {}", id, CurrentUser.label()) }
    }

    @Transactional
    fun delete(id: Long) {
        val category = findById(id)
        if (courseRepository.existsByCategoryId(id)) {
            log.warn("Remoção da categoria {} negada: ainda possui cursos (usuário {})", id, CurrentUser.label())
            throw ConflictException("A categoria $id possui cursos. Mova ou remova os cursos antes de excluí-la.")
        }
        repository.delete(category)
        log.warn("Categoria {} removida por {}", id, CurrentUser.label())
    }

    companion object {
        private val log = LoggerFactory.getLogger(CategoryService::class.java)
        private val SORTABLE_FIELDS = mapOf("id" to "id", "name" to "name")
    }
}
