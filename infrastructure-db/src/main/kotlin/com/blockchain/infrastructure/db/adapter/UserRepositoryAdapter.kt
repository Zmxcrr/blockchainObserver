package com.blockchain.infrastructure.db.adapter

import com.blockchain.domain.entity.User
import com.blockchain.domain.port.UserRepositoryPort
import com.blockchain.infrastructure.db.mapper.toDomain
import com.blockchain.infrastructure.db.mapper.toEntity
import com.blockchain.infrastructure.db.repository.UserR2dbcRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class UserRepositoryAdapter(
    private val userR2dbcRepository: UserR2dbcRepository
) : UserRepositoryPort {

    override fun save(user: User): Mono<User> {
        val entity = user.toEntity(isNew = true)
        return userR2dbcRepository.save(entity).map { it.toDomain() }
    }

    override fun findById(id: UUID): Mono<User> =
        userR2dbcRepository.findById(id).map { it.toDomain() }

    override fun findByEmail(email: String): Mono<User> =
        userR2dbcRepository.findByEmail(email).map { it.toDomain() }

    override fun existsByEmail(email: String): Mono<Boolean> =
        userR2dbcRepository.existsByEmail(email)

    override fun deleteById(id: UUID): Mono<Void> =
        userR2dbcRepository.deleteById(id)
}