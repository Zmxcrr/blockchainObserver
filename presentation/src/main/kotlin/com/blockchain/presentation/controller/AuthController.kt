package com.blockchain.presentation.controller

import com.blockchain.application.dto.AuthTokenResponse
import com.blockchain.application.dto.LoginRequest
import com.blockchain.application.dto.RegisterRequest
import com.blockchain.application.usecase.LoginUserUseCase
import com.blockchain.application.usecase.RegisterUserUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val registerUserUseCase: RegisterUserUseCase,
    private val loginUserUseCase: LoginUserUseCase
) {
    @PostMapping("/register")
    suspend fun register(@RequestBody request: RegisterRequest): ResponseEntity<AuthTokenResponse> {
        val tokenResponse = registerUserUseCase.execute(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse)
    }

    @PostMapping("/login")
    suspend fun login(@RequestBody request: LoginRequest): ResponseEntity<AuthTokenResponse> {
        val tokenResponse = loginUserUseCase.execute(request)
        return ResponseEntity.ok(tokenResponse)
    }
}