package br.pucpr.authserver.enrollments

import br.pucpr.authserver.enrollments.requests.EnrollmentRequest
import br.pucpr.authserver.enrollments.requests.UpdateEnrollmentStatusRequest
import br.pucpr.authserver.enrollments.responses.EnrollmentResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

/** Endpoints que associam (e desassociam) estudantes e cursos. */
@RestController
@RequestMapping("/enrollments")
@Validated
class EnrollmentController(private val service: EnrollmentService) {

    @PostMapping
    fun enroll(@Valid @RequestBody request: EnrollmentRequest): ResponseEntity<EnrollmentResponse> =
        service.enroll(request.studentId!!, request.courseId!!)
            .let { ResponseEntity.status(HttpStatus.CREATED).body(EnrollmentResponse(it)) }

    /**
     * Consulta com filtros e ordenação por parâmetros de query. Todos são opcionais.
     * Ex.: `GET /enrollments?studentId=1&status=ACTIVE&sortBy=course`
     */
    @GetMapping
    fun search(
        @RequestParam(required = false) @Positive studentId: Long?,
        @RequestParam(required = false) @Positive courseId: Long?,
        @RequestParam(required = false) status: EnrollmentStatus?,
        @RequestParam(required = false) sortBy: String?,
        @RequestParam(required = false) sortDir: String?
    ): ResponseEntity<List<EnrollmentResponse>> =
        service.search(studentId, courseId, status, sortBy, sortDir)
            .map { EnrollmentResponse(it) }
            .let { ResponseEntity.ok(it) }

    @GetMapping("/{id}")
    fun getById(@Positive @PathVariable id: Long): ResponseEntity<EnrollmentResponse> =
        ResponseEntity.ok(EnrollmentResponse(service.findById(id)))

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    fun updateStatus(
        @Positive @PathVariable id: Long,
        @Valid @RequestBody request: UpdateEnrollmentStatusRequest
    ): ResponseEntity<EnrollmentResponse> =
        service.updateStatus(id, request.status!!)
            .let { ResponseEntity.ok(EnrollmentResponse(it)) }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    fun unenroll(@Positive @PathVariable id: Long): ResponseEntity<Void> {
        service.unenroll(id)
        return ResponseEntity.noContent().build()
    }
}
