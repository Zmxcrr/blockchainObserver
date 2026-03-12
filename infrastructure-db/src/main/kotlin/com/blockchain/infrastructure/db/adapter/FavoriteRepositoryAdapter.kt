package com.blockchain.infrastructure.db.adapter

import com.blockchain.domain.entity.FavoriteAddress
import com.blockchain.domain.enum.Network
import com.blockchain.domain.port.FavoriteRepositoryPort
import com.blockchain.infrastructure.db.mapper.toDomain
import com.blockchain.infrastructure.db.mapper.toEntity
import com.blockchain.infrastructure.db.repository.FavoriteR2dbcRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class FavoriteRepositoryAdapter(
    private val favoriteR2dbcRepository: FavoriteR2dbcRepository
) : FavoriteRepositoryPort {

    override fun save(favorite: FavoriteAddress): Mono<FavoriteAddress> {
        return favoriteR2dbcRepository.save(favorite.toEntity(isNew = true))
            .map { it.toDomain() }
    }

    override fun findByUserId(userId: UUID): Flux<FavoriteAddress> =
        favoriteR2dbcRepository.findByUserId(userId).map { it.toDomain() }

    override fun findByIdAndUserId(id: UUID, userId: UUID): Mono<FavoriteAddress> =
        favoriteR2dbcRepository.findByIdAndUserId(id, userId).map { it.toDomain() }

    override fun findByUserIdAndAddressAndNetwork(
        userId: UUID,
        address: String,
        network: Network
    ): Mono<FavoriteAddress> =
        favoriteR2dbcRepository.findByUserIdAndAddressAndNetwork(userId, address, network.name)
            .map { it.toDomain() }

    override fun deleteById(id: UUID): Mono<Void> =
        favoriteR2dbcRepository.deleteById(id)

    override fun existsByUserIdAndAddressAndNetwork(
        userId: UUID,
        address: String,
        network: Network
    ): Mono<Boolean> =
        favoriteR2dbcRepository.existsByUserIdAndAddressAndNetwork(userId, address, network.name)
}