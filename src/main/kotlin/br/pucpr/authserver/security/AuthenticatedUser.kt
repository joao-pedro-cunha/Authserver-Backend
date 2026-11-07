package br.pucpr.authserver.security

/** Usuário extraído de um token JWT válido. É o `principal` do Spring Security. */
data class AuthenticatedUser(
    val id: Long,
    val email: String,
    val roles: Set<String>
)
