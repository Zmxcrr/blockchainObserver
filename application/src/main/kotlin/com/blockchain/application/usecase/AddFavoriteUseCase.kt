package com.blockchain.application.usecase

import com.blockchain.application.dto.AddFavoriteRequest
import com.blockchain.application.dto.FavoriteResponse
import com.blockchain.application.service.FavoriteService
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class AddFavoriteUseCase(private val favoriteService: FavoriteService) {
    fun execute(userId: UUID, request: AddFavoriteRequest): Mono<FavoriteResponse> =
        favoriteService.addFavorite(userId, request)
}