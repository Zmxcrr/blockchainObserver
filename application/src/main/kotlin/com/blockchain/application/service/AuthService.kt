package com.blockchain.application.service

import com.blockchain.application.dto.AuthTokenResponse
import com.blockchain.application.dto.LoginRequest
import com.blockchain.application.dto.RegisterRequest
import com.blockchain.application.port.JwtPort
import com.blockchain.domain.entity.User
import com.blockchain.domain.port.UserRepositoryPort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AuthService(
    private val userRepositoryPort: UserRepositoryPort,
    private val passwordEncoder: PasswordEncoder,
    private val jwtPort: JwtPort
) {
    suspend fun register(request: RegisterRequest): AuthTokenResponse {
        val user = User(
            id = UUID.randomUUID(),
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            username = request.username
        )

        val saved = userRepositoryPort.save(user)
        val token = jwtPort.generateToken(saved.id.toString(), saved.email)

        return AuthTokenResponse(
            accessToken = token,
            userId = saved.id.toString(),
            email = saved.email
        )
    }

    suspend fun login(request: LoginRequest): AuthTokenResponse {
        val user = userRepositoryPort.findByEmail(request.email)
            ?: throw IllegalArgumentException("Invalid email or password")

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid email or password")
        }

        val token = jwtPort.generateToken(user.id.toString(), user.email)

        return AuthTokenResponse(
            accessToken = token,
            userId = user.id.toString(),
            email = user.email
        )
    }
}