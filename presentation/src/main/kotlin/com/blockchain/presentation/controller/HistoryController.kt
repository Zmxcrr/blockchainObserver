package com.blockchain.presentation.controller

import com.blockchain.application.dto.SearchHistoryResponse
import com.blockchain.application.service.HistoryService
import kotlinx.coroutines.flow.Flow
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/history")
class HistoryController(
    private val historyService: HistoryService
) {
    @GetMapping
    fun getHistory(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): Flow<SearchHistoryResponse> {
        val userId = UUID.fromString(userDetails.username)
        return historyService.getHistory(userId, page, size)
    }

    @DeleteMapping
    suspend fun clearHistory(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Void> {
        val userId = UUID.fromString(userDetails.username)
        historyService.clearHistory(userId)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{id}")
    suspend fun deleteHistoryItem(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: UUID
    ): ResponseEntity<Void> {
        val userId = UUID.fromString(userDetails.username)
        historyService.deleteHistoryItem(userId, id)
        return ResponseEntity.noContent().build()
    }
}