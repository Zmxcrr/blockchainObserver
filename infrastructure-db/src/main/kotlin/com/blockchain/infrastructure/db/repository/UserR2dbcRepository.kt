package com.blockchain.infrastructure.db.repository

import com.blockchain.infrastructure.db.entity.UserEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserR2dbcRepository : CoroutineCrudRepository<UserEntity, UUID> {
    suspend fun findByEmail(email: String): UserEntity?
    suspend fun existsByEmail(email: String): Boolean
}