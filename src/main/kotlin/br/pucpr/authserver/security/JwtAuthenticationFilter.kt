package br.pucpr.authserver.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Lê o header `Authorization: Bearer <token>` e, se o token for válido, autentica a requisição.
 *
 * Token ausente ou inválido NÃO bloqueia a requisição aqui: ela segue como anônima e quem
 * decide é a regra de cada endpoint (`@PreAuthorize`). Isso mantém os GET públicos.
 *
 * Atenção: esta classe NÃO é um @Component de propósito. Se fosse, o Spring Boot a registraria
 * também como filtro do servlet, fora da cadeia do Spring Security, e a autenticação se perderia.
 */
class JwtAuthenticationFilter(private val jwt: JWT) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = bearerToken(request)
        if (token != null) {
            val user = jwt.extract(token)
            if (user != null) {
                val authorities = user.roles.map { SimpleGrantedAuthority("ROLE_$it") }
                val context = SecurityContextHolder.createEmptyContext()
                context.authentication = UsernamePasswordAuthenticationToken(user, null, authorities)
                SecurityContextHolder.setContext(context)
            } else {
                log.warn("Token JWT inválido ou expirado em {} {}", request.method, request.requestURI)
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun bearerToken(request: HttpServletRequest): String? =
        request.getHeader("Authorization")
            ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
            ?.substring(7)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    companion object {
        private val log = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)
    }
}
