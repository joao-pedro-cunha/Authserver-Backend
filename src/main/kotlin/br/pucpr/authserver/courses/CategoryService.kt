package br.pucpr.authserver.courses

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CategoryService(private val categoryRepository: CategoryRepository) {
    private val log = LoggerFactory.getLogger(CategoryService::class.java)

    fun create(name: String): Category {
        log.info("Criando nova categoria: '$name'")
        return categoryRepository.save(Category(name = name))
    }

    fun listAll(): List<Category> {
        log.info("Listando todas as categorias")
        return categoryRepository.findAll()
    }
}