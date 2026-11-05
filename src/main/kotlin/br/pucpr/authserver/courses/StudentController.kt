package br.pucpr.authserver.courses

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/students")
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
class StudentController(private val studentService: StudentService) {

    @PostMapping
    fun create(@RequestBody request: StudentRequest): ResponseEntity<Student> =
        ResponseEntity.status(HttpStatus.CREATED).body(studentService.create(request.name, request.email))

    @GetMapping
    fun list(
        @RequestParam(required = false) name: String?,
        @RequestParam(defaultValue = "name") sortBy: String
    ): ResponseEntity<List<Student>> =
        ResponseEntity.ok(studentService.listAll(name, sortBy))

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<Student> =
        ResponseEntity.ok(studentService.findById(id))

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: StudentRequest): ResponseEntity<Student> =
        ResponseEntity.ok(studentService.update(id, request.name, request.email))

    @DeleteMapping("/{id}")
    @Operation(security = [SecurityRequirement(name = "bearerAuth")])
    fun delete(
        @PathVariable id: Long,
        @RequestHeader("Authorization", required = false) token: String?
    ): ResponseEntity<Void> {
        if (token.isNullOrBlank()) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Acesso negado. Token em falta.")
        }

        studentService.delete(id)
        return ResponseEntity.noContent().build()
    }
}

data class StudentRequest(val name: String, val email: String)