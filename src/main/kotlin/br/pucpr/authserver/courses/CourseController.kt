package br.pucpr.authserver.courses

import br.pucpr.authserver.courses.requests.CourseRequest
import br.pucpr.authserver.courses.responses.CourseResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/courses")
@Validated
class CourseController(private val service: CourseService) {

    @PostMapping
    fun insert(@Valid @RequestBody request: CourseRequest): ResponseEntity<CourseResponse> =
        service.insert(request.title!!, request.workload!!, request.categoryId!!)
            .let { ResponseEntity.status(HttpStatus.CREATED).body(CourseResponse(it)) }

    /**
     * Consulta com filtros e ordenação por parâmetros de query. Todos são opcionais.
     * Ex.: `GET /courses?title=kotlin&categoryId=1&minWorkload=20&sortBy=workload&sortDir=DESC`
     */
    @GetMapping
    fun search(
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) @Positive categoryId: Long?,
        @RequestParam(required = false) @Min(0) minWorkload: Int?,
        @RequestParam(required = false) @Min(0) maxWorkload: Int?,
        @RequestParam(required = false) sortBy: String?,
        @RequestParam(required = false) sortDir: String?
    ): ResponseEntity<List<CourseResponse>> =
        service.search(title, categoryId, minWorkload, maxWorkload, sortBy, sortDir)
            .map { CourseResponse(it) }
            .let { ResponseEntity.ok(it) }

    @GetMapping("/{id}")
    fun getById(@Positive @PathVariable id: Long): ResponseEntity<CourseResponse> =
        ResponseEntity.ok(CourseResponse(service.findById(id)))

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    fun update(
        @Positive @PathVariable id: Long,
        @Valid @RequestBody request: CourseRequest
    ): ResponseEntity<CourseResponse> =
        service.update(id, request.title!!, request.workload!!, request.categoryId!!)
            .let { ResponseEntity.ok(CourseResponse(it)) }

    /** Associa o curso a outra categoria. */
    @PutMapping("/{id}/category/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    fun changeCategory(
        @Positive @PathVariable id: Long,
        @Positive @PathVariable categoryId: Long
    ): ResponseEntity<CourseResponse> =
        service.changeCategory(id, categoryId)
            .let { ResponseEntity.ok(CourseResponse(it)) }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    fun delete(@Positive @PathVariable id: Long): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }
}
