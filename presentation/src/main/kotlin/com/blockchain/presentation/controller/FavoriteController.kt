package com.blockchain.presentation.controller

import com.blockchain.application.dto.AddFavoriteRequest
import com.blockchain.application.dto.FavoriteResponse
import com.blockchain.application.usecase.AddFavoriteUseCase
import com.blockchain.application.service.FavoriteService
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/favorites")
class FavoriteController(
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val favoriteService: FavoriteService
) {
    @PostMapping
    suspend fun addFavorite(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: AddFavoriteRequest
    ): ResponseEntity<FavoriteResponse> {
        val userId = UUID.fromString(userDetails.username)
        val response = addFavoriteUseCase.execute(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun getFavorites(
        @AuthenticationPrincipal userDetails: UserDetails
    ): Flow<FavoriteResponse> {
        val userId = UUID.fromString(userDetails.username)
        return favoriteService.getFavorites(userId)
    }

    @DeleteMapping("/{id}")
    suspend fun removeFavorite(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: UUID
    ): ResponseEntity<Void> {
        val userId = UUID.fromString(userDetails.username)
        favoriteService.removeFavorite(userId, id)
        return ResponseEntity.noContent().build()
    }
}