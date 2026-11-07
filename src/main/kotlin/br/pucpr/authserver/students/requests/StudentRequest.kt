package br.pucpr.authserver.students.requests

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class StudentRequest(
    @NotBlank
    @Size(max = 255)
    val name: String?,

    @NotBlank
    @Email
    @Size(max = 255)
    val email: String?
)
