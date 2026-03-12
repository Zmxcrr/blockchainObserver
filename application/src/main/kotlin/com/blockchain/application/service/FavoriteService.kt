package com.blockchain.application.service

import com.blockchain.application.dto.AddFavoriteRequest
import com.blockchain.application.dto.FavoriteResponse
import com.blockchain.domain.entity.FavoriteAddress
import com.blockchain.domain.port.FavoriteRepositoryPort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class FavoriteService(
    private val favoriteRepositoryPort: FavoriteRepositoryPort
) {
    suspend fun addFavorite(userId: UUID, request: AddFavoriteRequest): FavoriteResponse {
        val exists = favoriteRepositoryPort.existsByUserIdAndAddressAndNetwork(
            userId = userId,
            address = request.address,
            network = request.network
        )

        if (exists) {
            throw IllegalArgumentException("Address already in favorites")
        }

        val favorite = FavoriteAddress(
            id = UUID.randomUUID(),
            userId = userId,
            address = request.address,
            network = request.network,
            label = request.label
        )

        val saved = favoriteRepositoryPort.save(favorite)
        return saved.toResponse()
    }

    fun getFavorites(userId: UUID): Flow<FavoriteResponse> {
        return favoriteRepositoryPort.findByUserId(userId).map { it.toResponse() }
    }

    suspend fun removeFavorite(userId: UUID, favoriteId: UUID) {
        val favorite = favoriteRepositoryPort.findByIdAndUserId(id = favoriteId, userId = userId)
            ?: throw IllegalArgumentException("Favorite not found for this user")

        favoriteRepositoryPort.deleteById(favorite.id)
    }

    private fun FavoriteAddress.toResponse() = FavoriteResponse(
        id = id.toString(),
        address = address,
        network = network,
        label = label
    )
}