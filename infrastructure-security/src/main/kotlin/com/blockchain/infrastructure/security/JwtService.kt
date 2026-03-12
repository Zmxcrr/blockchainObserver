package com.blockchain.infrastructure.security

import com.blockchain.application.port.JwtPort
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${app.jwt.secret}") private val secret: String,
    @Value("\${app.jwt.expiration-ms}") private val expirationMs: Long
) : JwtPort {

    private val signingKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8))
    }

    override fun generateToken(userId: String, email: String): String {
        val now = Date()
        val expiry = Date(now.time + expirationMs)
        return Jwts.builder()
            .subject(userId)
            .claim("email", email)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(signingKey)
            .compact()
    }

    override fun extractUserId(token: String): String? = runCatching {
        parseClaims(token).subject
    }.getOrNull()

    override fun extractEmail(token: String): String? = runCatching {
        parseClaims(token)["email"] as? String
    }.getOrNull()

    override fun isTokenValid(token: String): Boolean = runCatching {
        val claims = parseClaims(token)
        claims.expiration.after(Date())
    }.getOrDefault(false)

    private fun parseClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
}