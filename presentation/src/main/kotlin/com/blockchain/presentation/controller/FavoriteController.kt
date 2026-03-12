package com.blockchain.presentation.controller

import com.blockchain.application.dto.AddFavoriteRequest
import com.blockchain.application.dto.FavoriteResponse
import com.blockchain.application.usecase.AddFavoriteUseCase
import com.blockchain.application.service.FavoriteService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/api/favorites")
class FavoriteController(
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val favoriteService: FavoriteService
) {
    @PostMapping
    fun addFavorite(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: AddFavoriteRequest
    ): Mono<ResponseEntity<FavoriteResponse>> {
        val userId = UUID.fromString(userDetails.username)
        return addFavoriteUseCase.execute(userId, request)
            .map { ResponseEntity.status(HttpStatus.CREATED).body(it) }
    }

    @GetMapping
    fun getFavorites(
        @AuthenticationPrincipal userDetails: UserDetails
    ): Flux<FavoriteResponse> {
        val userId = UUID.fromString(userDetails.username)
        return favoriteService.getFavorites(userId)
    }

    @DeleteMapping("/{id}")
    fun removeFavorite(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: UUID
    ): Mono<ResponseEntity<Void>> {
        val userId = UUID.fromString(userDetails.username)
        return favoriteService.removeFavorite(userId, id)
            .thenReturn(ResponseEntity.noContent().build<Void>())
    }
}