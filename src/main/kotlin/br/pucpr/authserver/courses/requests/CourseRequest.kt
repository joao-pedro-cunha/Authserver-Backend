package br.pucpr.authserver.courses.requests

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class CourseRequest(
    @NotBlank
    @Size(max = 255)
    val title: String?,

    @NotNull
    @Positive
    val workload: Int?,

    @NotNull
    @Positive
    val categoryId: Long?
)
