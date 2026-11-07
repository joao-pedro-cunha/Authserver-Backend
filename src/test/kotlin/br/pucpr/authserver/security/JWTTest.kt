package br.pucpr.authserver.security

import br.pucpr.authserver.roles.Role
import br.pucpr.authserver.users.oneUser
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class JWTTest {
    private val secret = "um-segredo-de-teste-com-mais-de-32-caracteres"
    private val jwt = JWT(secret, 60_000)

    @Test
    fun `extract returns the user of a valid token`() {
        val user = oneUser(id = 7, roles = mutableSetOf(Role(name = "ADMIN", description = "Admin")))

        val result = jwt.extract(jwt.createToken(user)).shouldNotBeNull()

        result.id shouldBe 7L
        result.email shouldBe user.email
        result.roles shouldBe setOf("ADMIN")
    }

    @Test
    fun `extract rejects garbage`() {
        jwt.extract("isto-nao-e-um-token").shouldBeNull()
        jwt.extract("").shouldBeNull()
    }

    @Test
    fun `extract rejects a token signed with another key`() {
        val other = JWT("outro-segredo-de-teste-com-mais-de-32-caracteres", 60_000)

        jwt.extract(other.createToken(oneUser())).shouldBeNull()
    }

    @Test
    fun `extract rejects an expired token`() {
        val expired = JWT(secret, -1_000)

        jwt.extract(expired.createToken(oneUser())).shouldBeNull()
    }
}
