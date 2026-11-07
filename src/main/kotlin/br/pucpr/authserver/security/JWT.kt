package br.pucpr.authserver.security

import br.pucpr.authserver.users.User
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

/**
 * Criação e validação de tokens JWT (HS256).
 *
 * A chave vem de `security.jwt.secret` (variável de ambiente `JWT_SECRET`, mínimo 32 caracteres).
 * Se não for informada, gera-se uma chave temporária: os tokens deixam de valer a cada reinício.
 */
@Component
class JWT(
    @Value("\${security.jwt.secret:}") secret: String,
    @Value("\${security.jwt.expiration-ms:86400000}") private val expirationMs: Long
) {
    private val key: SecretKey = buildKey(secret)

    fun createToken(user: User): String {
        val now = Date()
        return Jwts.builder()
            .subject(user.id.toString())
            .claim("email", user.email)
            .claim("roles", user.roles.map { it.name })
            .issuedAt(now)
            .expiration(Date(now.time + expirationMs))
            .signWith(key)
            .compact()
    }

    /** Valida assinatura e validade do token. Devolve `null` se o token for inválido ou expirado. */
    fun extract(token: String): AuthenticatedUser? = try {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

        AuthenticatedUser(
            id = claims.subject.toLong(),
            email = claims["email"] as String,
            roles = (claims["roles"] as? List<*>)?.map { it.toString() }?.toSet() ?: emptySet()
        )
    } catch (e: JwtException) {
        log.debug("Token rejeitado: {}", e.javaClass.simpleName)
        null
    } catch (e: IllegalArgumentException) {
        log.debug("Token rejeitado: formato inválido")
        null
    }

    private fun buildKey(secret: String): SecretKey {
        if (secret.isBlank()) {
            log.warn("security.jwt.secret não definido: usando chave temporária. Defina JWT_SECRET em produção!")
            return Jwts.SIG.HS256.key().build()
        }
        check(secret.toByteArray(Charsets.UTF_8).size >= 32) {
            "security.jwt.secret precisa ter pelo menos 32 caracteres"
        }
        return Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8))
    }

    companion object {
        private val log = LoggerFactory.getLogger(JWT::class.java)
    }
}
