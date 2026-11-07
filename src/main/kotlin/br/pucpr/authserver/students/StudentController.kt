package br.pucpr.authserver.students

import br.pucpr.authserver.students.requests.StudentRequest
import br.pucpr.authserver.students.responses.StudentResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/students")
@Validated
class StudentController(private val service: StudentService) {

    @PostMapping
    fun insert(@Valid @RequestBody request: StudentRequest): ResponseEntity<StudentResponse> =
        service.insert(request.name!!, request.email!!)
            .let { ResponseEntity.status(HttpStatus.CREATED).body(StudentResponse(it)) }

    /**
     * Consulta com filtros e ordenação por parâmetros de query. Todos são opcionais.
     * Ex.: `GET /students?name=ana&sortBy=email&sortDir=DESC`
     */
    @GetMapping
    fun search(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) email: String?,
        @RequestParam(required = false) sortBy: String?,
        @RequestParam(required = false) sortDir: String?
    ): ResponseEntity<List<StudentResponse>> =
        service.search(name, email, sortBy, sortDir)
            .map { StudentResponse(it) }
            .let { ResponseEntity.ok(it) }

    @GetMapping("/{id}")
    fun getById(@Positive @PathVariable id: Long): ResponseEntity<StudentResponse> =
        ResponseEntity.ok(StudentResponse(service.findById(id)))

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    fun update(
        @Positive @PathVariable id: Long,
        @Valid @RequestBody request: StudentRequest
    ): ResponseEntity<StudentResponse> =
        service.update(id, request.name!!, request.email!!)
            .let { ResponseEntity.ok(StudentResponse(it)) }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    fun delete(@Positive @PathVariable id: Long): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }
}
