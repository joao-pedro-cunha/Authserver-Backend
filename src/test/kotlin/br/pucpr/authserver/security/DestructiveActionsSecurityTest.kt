package br.pucpr.authserver.security

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Teste de ponta a ponta com o servidor real: prova que as ações destrutivas exigem um token JWT
 * válido, e que o fluxo completo (criar, associar, consultar, remover) funciona com ele.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.datasource.url=jdbc:h2:mem:securitytest"]
)
class DestructiveActionsSecurityTest {

    @Autowired
    lateinit var environment: Environment

    private val client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()

    // ---------- helpers ----------

    private fun send(method: String, path: String, body: String? = null, token: String? = null): HttpResponse<String> {
        val port = environment.getProperty("local.server.port")
        val builder = HttpRequest.newBuilder(URI.create("http://localhost:$port/api$path"))
        builder.method(
            method,
            if (body == null) HttpRequest.BodyPublishers.noBody() else HttpRequest.BodyPublishers.ofString(body)
        )
        if (body != null) builder.header("Content-Type", "application/json")
        if (token != null) builder.header("Authorization", "Bearer $token")
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString())
    }

    private fun login(): String {
        val response = send("POST", "/users/login", """{"email":"admin@authserver.com","password":"admin"}""")
        response.statusCode() shouldBe 200
        return Regex("\"token\"\\s*:\\s*\"([^\"]+)\"").find(response.body())!!.groupValues[1]
    }

    private fun idOf(response: HttpResponse<String>): Long {
        response.statusCode() shouldBe 201
        return Regex("\"id\"\\s*:\\s*(\\d+)").find(response.body())!!.groupValues[1].toLong()
    }

    // Todas as ações destrutivas (PUT, PATCH, DELETE) da API de estudantes, cursos, categorias e matrículas.
    private val destructiveRequests = listOf(
        Triple("PUT", "/students/999999", """{"name":"Ana","email":"ana@email.com"}"""),
        Triple("DELETE", "/students/999999", null),
        Triple("PUT", "/courses/999999", """{"title":"Kotlin","workload":40,"categoryId":1}"""),
        Triple("PUT", "/courses/999999/category/1", null),
        Triple("DELETE", "/courses/999999", null),
        Triple("PUT", "/categories/999999", """{"name":"Backend"}"""),
        Triple("DELETE", "/categories/999999", null),
        Triple("PATCH", "/enrollments/999999/status", """{"status":"CANCELLED"}"""),
        Triple("DELETE", "/enrollments/999999", null)
    )

    // ---------- tests ----------

    @Test
    fun `reads are public`() {
        listOf("/students", "/courses", "/categories", "/enrollments").forEach {
            send("GET", it).statusCode() shouldBe 200
        }
    }

    /** Envia todas as ações destrutivas e devolve as que NÃO responderam com o status esperado. */
    private fun unexpected(expected: Int, token: String?): List<String> =
        destructiveRequests.mapNotNull { (method, path, body) ->
            val status = send(method, path, body, token).statusCode()
            if (status == expected) null else "$method $path -> $status (esperado $expected)"
        }

    @Test
    fun `destructive actions are rejected without a token`() {
        unexpected(expected = 401, token = null) shouldBe emptyList<String>()
    }

    @Test
    fun `destructive actions are rejected with an invalid token`() {
        unexpected(expected = 401, token = "token.invalido.aqui") shouldBe emptyList<String>()
    }

    @Test
    fun `destructive actions pass the security gate with a valid token`() {
        // Os ids não existem: 404 prova que a requisição passou da segurança e chegou ao serviço.
        unexpected(expected = 404, token = login()) shouldBe emptyList<String>()
    }

    @Test
    fun `full flow - create, associate, query and remove`() {
        val token = login()

        val categoryId = idOf(send("POST", "/categories", """{"name":"Fluxo completo"}"""))
        val courseId = idOf(send("POST", "/courses", """{"title":"Kotlin Avancado","workload":60,"categoryId":$categoryId}"""))
        val studentId = idOf(send("POST", "/students", """{"name":"Ana Fluxo","email":"ana.fluxo@email.com"}"""))

        // e-mail duplicado
        send("POST", "/students", """{"name":"Outra","email":"ANA.FLUXO@email.com"}""").statusCode() shouldBe 409

        // associação estudante <-> curso
        val enrollmentId = idOf(send("POST", "/enrollments", """{"studentId":$studentId,"courseId":$courseId}"""))
        send("POST", "/enrollments", """{"studentId":$studentId,"courseId":$courseId}""").statusCode() shouldBe 409

        // consulta com filtro + ordenação
        val found = send("GET", "/courses?title=kotlin&categoryId=$categoryId&minWorkload=50&sortBy=workload&sortDir=DESC")
        found.statusCode() shouldBe 200
        found.body().contains("Kotlin Avancado") shouldBe true
        send("GET", "/courses?title=kotlin&categoryId=$categoryId&minWorkload=100").body().contains("Kotlin Avancado") shouldBe false
        send("GET", "/courses?sortBy=senha").statusCode() shouldBe 400
        send("GET", "/enrollments?studentId=$studentId&status=ACTIVE").body().contains("Ana Fluxo") shouldBe true

        // não dá para apagar a categoria enquanto houver curso
        send("DELETE", "/categories/$categoryId", token = token).statusCode() shouldBe 409

        // remoção, agora autenticada
        send("PATCH", "/enrollments/$enrollmentId/status", """{"status":"COMPLETED"}""", token).statusCode() shouldBe 200
        send("DELETE", "/enrollments/$enrollmentId", token = token).statusCode() shouldBe 204
        send("DELETE", "/courses/$courseId", token = token).statusCode() shouldBe 204
        send("DELETE", "/categories/$categoryId", token = token).statusCode() shouldBe 204
        send("DELETE", "/students/$studentId", token = token).statusCode() shouldBe 204
        send("GET", "/students/$studentId").statusCode() shouldBe 404
    }
}
