package com.blockchain.application.service

import com.blockchain.domain.entity.User
import com.blockchain.domain.port.UserRepositoryPort
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class UserService(
    private val userRepositoryPort: UserRepositoryPort
) {
    fun getUserById(id: UUID): Mono<User> {
        return userRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("User not found: $id")))
    }

    fun getUserByEmail(email: String): Mono<User> {
        return userRepositoryPort.findByEmail(email)
            .switchIfEmpty(Mono.error(NoSuchElementException("User not found: $email")))
    }
}