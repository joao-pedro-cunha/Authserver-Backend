package br.pucpr.authserver.categories

import br.pucpr.authserver.categories.requests.CategoryRequest
import br.pucpr.authserver.categories.responses.CategoryResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/categories")
@Validated
class CategoryController(private val service: CategoryService) {

    @PostMapping
    fun insert(@Valid @RequestBody request: CategoryRequest): ResponseEntity<CategoryResponse> =
        service.insert(request.name!!)
            .let { ResponseEntity.status(HttpStatus.CREATED).body(CategoryResponse(it)) }

    /** Ex.: `GET /categories?sortBy=name&sortDir=DESC` */
    @GetMapping
    fun list(
        @RequestParam(required = false) sortBy: String?,
        @RequestParam(required = false) sortDir: String?
    ): ResponseEntity<List<CategoryResponse>> =
        service.findAll(sortBy, sortDir)
            .map { CategoryResponse(it) }
            .let { ResponseEntity.ok(it) }

    @GetMapping("/{id}")
    fun getById(@Positive @PathVariable id: Long): ResponseEntity<CategoryResponse> =
        ResponseEntity.ok(CategoryResponse(service.findById(id)))

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    fun update(
        @Positive @PathVariable id: Long,
        @Valid @RequestBody request: CategoryRequest
    ): ResponseEntity<CategoryResponse> =
        ResponseEntity.ok(CategoryResponse(service.update(id, request.name!!)))

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    fun delete(@Positive @PathVariable id: Long): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }
}
