package br.pucpr.authserver.categories.requests

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CategoryRequest(
    @NotBlank
    @Size(max = 255)
    val name: String?
)
