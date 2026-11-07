package br.pucpr.authserver.courses.responses

import br.pucpr.authserver.courses.Course

data class CourseResponse(
    val id: Long,
    val title: String,
    val workload: Int,
    val categoryId: Long,
    val categoryName: String
) {
    constructor(course: Course) : this(
        id = course.id!!,
        title = course.title,
        workload = course.workload,
        categoryId = course.category.id!!,
        categoryName = course.category.name
    )
}
