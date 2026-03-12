package com.blockchain.application.usecase

import com.blockchain.application.dto.AuthTokenResponse
import com.blockchain.application.dto.RegisterRequest
import com.blockchain.application.service.AuthService
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class RegisterUserUseCase(private val authService: AuthService) {
    fun execute(request: RegisterRequest): Mono<AuthTokenResponse> = authService.register(request)
}