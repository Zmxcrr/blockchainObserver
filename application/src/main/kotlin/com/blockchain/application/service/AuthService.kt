package com.blockchain.application.service

import com.blockchain.application.dto.AuthTokenResponse
import com.blockchain.application.dto.LoginRequest
import com.blockchain.application.dto.RegisterRequest
import com.blockchain.application.port.JwtPort
import com.blockchain.domain.entity.User
import com.blockchain.domain.port.UserRepositoryPort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class AuthService(
    private val userRepositoryPort: UserRepositoryPort,
    private val passwordEncoder: PasswordEncoder,
    private val jwtPort: JwtPort
) {
    fun register(request: RegisterRequest): Mono<AuthTokenResponse> {
        val user = User(
            id = UUID.randomUUID(),
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            username = request.username
        )

        return userRepositoryPort.save(user)
            .map { saved ->
                val token = jwtPort.generateToken(saved.id.toString(), saved.email)
                AuthTokenResponse(
                    accessToken = token,
                    userId = saved.id.toString(),
                    email = saved.email
                )
            }
    }

    fun login(request: LoginRequest): Mono<AuthTokenResponse> {
        return userRepositoryPort.findByEmail(request.email)
            .switchIfEmpty(Mono.error(IllegalArgumentException("Invalid email or password")))
            .flatMap { user ->
                if (!passwordEncoder.matches(request.password, user.passwordHash)) {
                    Mono.error(IllegalArgumentException("Invalid email or password"))
                } else {
                    val token = jwtPort.generateToken(user.id.toString(), user.email)
                    Mono.just(
                        AuthTokenResponse(
                            accessToken = token,
                            userId = user.id.toString(),
                            email = user.email
                        )
                    )
                }
            }
    }
}