package com.blockchain.application.service

import com.blockchain.domain.entity.User
import com.blockchain.domain.port.UserRepositoryPort
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepositoryPort: UserRepositoryPort
) {
    suspend fun getUserById(id: UUID): User {
        return userRepositoryPort.findById(id)
            ?: throw NoSuchElementException("User not found: $id")
    }

    suspend fun getUserByEmail(email: String): User {
        return userRepositoryPort.findByEmail(email)
            ?: throw NoSuchElementException("User not found: $email")
    }
}