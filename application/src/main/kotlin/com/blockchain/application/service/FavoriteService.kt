package com.blockchain.application.service

import com.blockchain.application.dto.AddFavoriteRequest
import com.blockchain.application.dto.FavoriteResponse
import com.blockchain.domain.entity.FavoriteAddress
import com.blockchain.domain.port.FavoriteRepositoryPort
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class FavoriteService(
    private val favoriteRepositoryPort: FavoriteRepositoryPort
) {
    fun addFavorite(userId: UUID, request: AddFavoriteRequest): Mono<FavoriteResponse> {
        return favoriteRepositoryPort.existsByUserIdAndAddressAndNetwork(userId, request.address, request.network)
            .flatMap { exists ->
                if (exists) Mono.error(IllegalArgumentException("Address already in favorites"))
                else {
                    val favorite = FavoriteAddress(
                        id = UUID.randomUUID(),
                        userId = userId,
                        address = request.address,
                        network = request.network,
                        label = request.label
                    )
                    favoriteRepositoryPort.save(favorite)
                }
            }
            .map { it.toResponse() }
    }

    fun getFavorites(userId: UUID): Flux<FavoriteResponse> {
        return favoriteRepositoryPort.findByUserId(userId).map { it.toResponse() }
    }

    fun removeFavorite(userId: UUID, favoriteId: UUID): Mono<Void> {
        return favoriteRepositoryPort.findByIdAndUserId(id = favoriteId, userId = userId)
            .switchIfEmpty(Mono.error(IllegalArgumentException("Favorite not found for this user")))
            .flatMap { favoriteRepositoryPort.deleteById(favoriteId) }
    }


    private fun FavoriteAddress.toResponse() = FavoriteResponse(
        id = id.toString(),
        address = address,
        network = network,
        label = label
    )
}