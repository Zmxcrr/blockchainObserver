package com.blockchain.application.port

interface JwtPort {
    fun generateToken(userId: String, email: String): String
    fun extractUserId(token: String): String?
    fun extractEmail(token: String): String?
    fun isTokenValid(token: String): Boolean
}