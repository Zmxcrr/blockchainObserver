package com.blockchain.domain.port

import com.blockchain.domain.entity.User
import reactor.core.publisher.Mono
import java.util.UUID

interface UserRepositoryPort {
    fun save(user: User): Mono<User>
    fun findById(id: UUID): Mono<User>
    fun findByEmail(email: String): Mono<User>
    fun existsByEmail(email: String): Mono<Boolean>
    fun deleteById(id: UUID): Mono<Void>
}