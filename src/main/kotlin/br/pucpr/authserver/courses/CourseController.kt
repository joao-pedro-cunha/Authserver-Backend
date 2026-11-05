package br.pucpr.authserver.courses

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/courses")
class CourseController(private val courseService: CourseService) {

    @PostMapping
    fun create(@RequestBody request: CourseRequest): ResponseEntity<Course> =
        ResponseEntity.status(HttpStatus.CREATED).body(courseService.create(request.title, request.workload, request.categoryId))

    @GetMapping
    fun list(
        @RequestParam(required = false) title: String?,
        @RequestParam(defaultValue = "title") sortBy: String
    ): ResponseEntity<List<Course>> =
        ResponseEntity.ok(courseService.listAll(title, sortBy))

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<Course> =
        ResponseEntity.ok(courseService.findById(id))

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: CourseRequest): ResponseEntity<Course> =
        ResponseEntity.ok(courseService.update(id, request.title, request.workload))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        courseService.delete(id)
        return ResponseEntity.noContent().build()
    }
}

data class CourseRequest(val title: String, val workload: Int, val categoryId: Long)