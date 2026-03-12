package com.blockchain.domain.entity

import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val username: String,
    val createdAt: java.time.Instant = java.time.Instant.now()
)