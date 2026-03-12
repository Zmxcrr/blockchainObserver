package com.blockchain.application.dto

data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthTokenResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val userId: String,
    val email: String
)