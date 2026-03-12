package com.blockchain.application.usecase

import com.blockchain.application.dto.AddFavoriteRequest
import com.blockchain.application.dto.FavoriteResponse
import com.blockchain.application.service.FavoriteService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AddFavoriteUseCase(private val favoriteService: FavoriteService) {
    suspend fun execute(userId: UUID, request: AddFavoriteRequest): FavoriteResponse =
        favoriteService.addFavorite(userId, request)
}