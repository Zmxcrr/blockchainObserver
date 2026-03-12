package com.blockchain.infrastructure.db.repository

import com.blockchain.infrastructure.db.entity.UserEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
interface UserR2dbcRepository : ReactiveCrudRepository<UserEntity, UUID> {
    fun findByEmail(email: String): Mono<UserEntity>
    fun existsByEmail(email: String): Mono<Boolean>
}