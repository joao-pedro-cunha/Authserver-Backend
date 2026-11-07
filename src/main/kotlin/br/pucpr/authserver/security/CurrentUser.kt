package br.pucpr.authserver.security

import org.springframework.security.core.context.SecurityContextHolder

/** Acesso ao usuário da requisição atual, usado principalmente nos logs de auditoria. */
object CurrentUser {
    fun get(): AuthenticatedUser? =
        SecurityContextHolder.getContext().authentication?.principal as? AuthenticatedUser

    /** Texto curto para logs, ex.: `admin@authserver.com (id=1)`. */
    fun label(): String = get()?.let { "${it.email} (id=${it.id})" } ?: "anonymous"
}
