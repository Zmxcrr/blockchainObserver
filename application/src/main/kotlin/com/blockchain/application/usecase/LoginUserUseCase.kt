package com.blockchain.application.usecase

import com.blockchain.application.dto.AuthTokenResponse
import com.blockchain.application.dto.LoginRequest
import com.blockchain.application.service.AuthService
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class LoginUserUseCase(private val authService: AuthService) {
    fun execute(request: LoginRequest): Mono<AuthTokenResponse> = authService.login(request)
}