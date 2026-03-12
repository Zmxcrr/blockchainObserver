package com.blockchain.presentation.controller

import com.blockchain.application.dto.AuthTokenResponse
import com.blockchain.application.dto.LoginRequest
import com.blockchain.application.dto.RegisterRequest
import com.blockchain.application.usecase.LoginUserUseCase
import com.blockchain.application.usecase.RegisterUserUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val registerUserUseCase: RegisterUserUseCase,
    private val loginUserUseCase: LoginUserUseCase
) {
    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): Mono<ResponseEntity<AuthTokenResponse>> {
        return registerUserUseCase.execute(request)
            .map { ResponseEntity.status(HttpStatus.CREATED).body(it) }
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): Mono<ResponseEntity<AuthTokenResponse>> {
        return loginUserUseCase.execute(request)
            .map { ResponseEntity.ok(it) }
    }
}