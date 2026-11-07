package br.pucpr.authserver.common

import br.pucpr.authserver.exceptions.BadRequestException
import br.pucpr.authserver.users.SortDir
import org.springframework.data.domain.Sort

/**
 * Converte os parâmetros de query `sortBy` e `sortDir` em um [Sort] seguro.
 *
 * O campo vem de uma lista branca (`allowed`: nome público -> propriedade da entidade),
 * evitando que o cliente ordene por qualquer propriedade e provoque erros 500.
 */
object Sorting {
    fun resolve(
        sortBy: String?,
        sortDir: String?,
        allowed: Map<String, String>,
        default: String
    ): Sort {
        val field = sortBy?.takeIf { it.isNotBlank() } ?: default
        val property = allowed[field]
            ?: throw BadRequestException(
                "Campo de ordenação inválido: '$field'. Use um de: ${allowed.keys.joinToString()}"
            )
        val direction = when (SortDir.findOrThrow(sortDir ?: "ASC")) {
            SortDir.ASC -> Sort.Direction.ASC
            SortDir.DESC -> Sort.Direction.DESC
        }
        return Sort.by(direction, property)
    }
}
