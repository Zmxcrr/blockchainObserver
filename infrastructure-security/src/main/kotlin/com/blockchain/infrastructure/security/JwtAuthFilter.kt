package com.blockchain.infrastructure.security

import com.blockchain.application.port.JwtPort
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class JwtAuthFilter(
    private val jwtPort: JwtPort,
    private val userDetailsService: ReactiveUserDetailsService
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val authHeader = exchange.request.headers.getFirst("Authorization")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange)
        }

        val token = authHeader.removePrefix("Bearer ")
        if (!jwtPort.isTokenValid(token)) {
            return chain.filter(exchange)
        }

        val userId = jwtPort.extractUserId(token) ?: return chain.filter(exchange)

        return userDetailsService.findByUsername(userId)
            .map { userDetails ->
                UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.authorities
                )
            }
            .flatMap { auth ->
                chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))
            }
    }
}