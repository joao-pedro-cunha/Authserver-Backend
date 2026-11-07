package br.pucpr.authserver.enrollments.requests

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class EnrollmentRequest(
    @NotNull
    @Positive
    val studentId: Long?,

    @NotNull
    @Positive
    val courseId: Long?
)
