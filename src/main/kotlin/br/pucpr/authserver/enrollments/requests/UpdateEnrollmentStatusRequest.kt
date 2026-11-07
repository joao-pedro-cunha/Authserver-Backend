package br.pucpr.authserver.enrollments.requests

import br.pucpr.authserver.enrollments.EnrollmentStatus
import jakarta.validation.constraints.NotNull

data class UpdateEnrollmentStatusRequest(
    @NotNull
    val status: EnrollmentStatus?
)
