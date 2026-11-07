package br.pucpr.authserver.students.responses

import br.pucpr.authserver.students.Student

data class StudentResponse(
    val id: Long,
    val name: String,
    val email: String
) {
    constructor(student: Student) : this(
        id = student.id!!,
        name = student.name,
        email = student.email
    )
}
