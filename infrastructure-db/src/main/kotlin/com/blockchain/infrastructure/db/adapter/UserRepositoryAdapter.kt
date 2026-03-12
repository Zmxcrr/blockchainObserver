package com.blockchain.infrastructure.db.adapter

import com.blockchain.domain.entity.User
import com.blockchain.domain.port.UserRepositoryPort
import com.blockchain.infrastructure.db.mapper.toDomain
import com.blockchain.infrastructure.db.mapper.toEntity
import com.blockchain.infrastructure.db.repository.UserR2dbcRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UserRepositoryAdapter(
    private val userR2dbcRepository: UserR2dbcRepository
) : UserRepositoryPort {

    override suspend fun save(user: User): User {
        val entity = user.toEntity(isNew = true)
        return userR2dbcRepository.save(entity).toDomain()
    }

    override suspend fun findById(id: UUID): User? =
        userR2dbcRepository.findById(id)?.toDomain()

    override suspend fun findByEmail(email: String): User? =
        userR2dbcRepository.findByEmail(email)?.toDomain()

    override suspend fun existsByEmail(email: String): Boolean =
        userR2dbcRepository.existsByEmail(email)

    override suspend fun deleteById(id: UUID) {
        userR2dbcRepository.deleteById(id)
    }
}