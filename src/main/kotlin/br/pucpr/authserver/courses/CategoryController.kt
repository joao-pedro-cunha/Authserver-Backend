package br.pucpr.authserver.courses

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/categories")
class CategoryController(private val categoryService: CategoryService) {

    @PostMapping
    fun create(@RequestBody request: CategoryRequest): ResponseEntity<Category> =
        ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request.name))

    @GetMapping
    fun list(): ResponseEntity<List<Category>> =
        ResponseEntity.ok(categoryService.listAll())
}

data class CategoryRequest(val name: String)