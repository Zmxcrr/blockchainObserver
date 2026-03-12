package com.blockchain.domain.port

import com.blockchain.domain.entity.User
import java.util.UUID

interface UserRepositoryPort {
    suspend fun save(user: User): User
    suspend fun findById(id: UUID): User?
    suspend fun findByEmail(email: String): User?
    suspend fun existsByEmail(email: String): Boolean
    suspend fun deleteById(id: UUID)
}