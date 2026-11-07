package br.pucpr.authserver.security

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * Segurança da API: stateless, autenticação por JWT e regras por método.
 *
 * As ações destrutivas (PUT, PATCH e DELETE) são protegidas com `@PreAuthorize("isAuthenticated()")`
 * diretamente nos controllers, e aparecem com cadeado no Swagger via `@SecurityRequirement`.
 *
 * A cadeia de filtros deixa tudo passar (`permitAll`) de propósito: o módulo de usuários não faz parte
 * deste trabalho e continua se comportando como antes. Quando ele for protegido, basta trocar
 * `anyRequest().permitAll()` por regras específicas.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
class SecurityConfig(private val jwt: JWT) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain = http
        .csrf { it.disable() } // API stateless com token no header: não usa cookie de sessão
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .headers { headers -> headers.frameOptions { it.sameOrigin() } } // o console do H2 usa frames
        .authorizeHttpRequests { it.anyRequest().permitAll() }
        .exceptionHandling { it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) }
        .addFilterBefore(JwtAuthenticationFilter(jwt), UsernamePasswordAuthenticationFilter::class.java)
        .build()

    /**
     * A autenticação é feita pelo JWT, não por usuário/senha do Spring. Declarar este bean impede o
     * Spring Boot de criar o usuário padrão com senha aleatória (aquela linha confusa no log).
     */
    @Bean
    fun userDetailsService(): UserDetailsService =
        UserDetailsService { username -> throw UsernameNotFoundException(username) }
}
