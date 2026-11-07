package br.pucpr.authserver.categories.responses

import br.pucpr.authserver.categories.Category

data class CategoryResponse(
    val id: Long,
    val name: String
) {
    constructor(category: Category) : this(
        id = category.id!!,
        name = category.name
    )
}
