package com.blockchain.domain.port

import com.blockchain.domain.entity.FavoriteAddress
import com.blockchain.domain.enum.Network
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface FavoriteRepositoryPort {
    suspend fun save(favorite: FavoriteAddress): FavoriteAddress
    fun findByUserId(userId: UUID): Flow<FavoriteAddress>

    
    suspend fun findByIdAndUserId(id: UUID, userId: UUID): FavoriteAddress?

    suspend fun findByUserIdAndAddressAndNetwork(userId: UUID, address: String, network: Network): FavoriteAddress?

    suspend fun deleteById(id: UUID)

    suspend fun existsByUserIdAndAddressAndNetwork(userId: UUID, address: String, network: Network): Boolean
}