package com.blockchain.infrastructure.db.adapter

import com.blockchain.domain.entity.FavoriteAddress
import com.blockchain.domain.enum.Network
import com.blockchain.domain.port.FavoriteRepositoryPort
import com.blockchain.infrastructure.db.mapper.toDomain
import com.blockchain.infrastructure.db.mapper.toEntity
import com.blockchain.infrastructure.db.repository.FavoriteR2dbcRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class FavoriteRepositoryAdapter(
    private val favoriteR2dbcRepository: FavoriteR2dbcRepository
) : FavoriteRepositoryPort {

    override suspend fun save(favorite: FavoriteAddress): FavoriteAddress {
        return favoriteR2dbcRepository.save(favorite.toEntity(isNew = true)).toDomain()
    }

    override fun findByUserId(userId: UUID): Flow<FavoriteAddress> =
        favoriteR2dbcRepository.findByUserId(userId).map { it.toDomain() }

    override suspend fun findByIdAndUserId(id: UUID, userId: UUID): FavoriteAddress? =
        favoriteR2dbcRepository.findByIdAndUserId(id, userId)?.toDomain()

    override suspend fun findByUserIdAndAddressAndNetwork(
        userId: UUID,
        address: String,
        network: Network
    ): FavoriteAddress? =
        favoriteR2dbcRepository.findByUserIdAndAddressAndNetwork(userId, address, network.name)?.toDomain()

    override suspend fun deleteById(id: UUID) {
        favoriteR2dbcRepository.deleteById(id)
    }

    override suspend fun existsByUserIdAndAddressAndNetwork(
        userId: UUID,
        address: String,
        network: Network
    ): Boolean =
        favoriteR2dbcRepository.existsByUserIdAndAddressAndNetwork(userId, address, network.name)
}