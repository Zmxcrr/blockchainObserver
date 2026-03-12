package com.blockchain.application.usecase

import com.blockchain.application.dto.AuthTokenResponse
import com.blockchain.application.dto.LoginRequest
import com.blockchain.application.service.AuthService
import org.springframework.stereotype.Component

@Component
class LoginUserUseCase(private val authService: AuthService) {
    suspend fun execute(request: LoginRequest): AuthTokenResponse =
        authService.login(request)
}