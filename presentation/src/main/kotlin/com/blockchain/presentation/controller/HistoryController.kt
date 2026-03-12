package com.blockchain.presentation.controller

import com.blockchain.application.dto.SearchHistoryResponse
import com.blockchain.application.service.HistoryService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
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
    ): Flux<SearchHistoryResponse> {
        val userId = UUID.fromString(userDetails.username)
        return historyService.getHistory(userId, page, size)
    }

    @DeleteMapping
    fun clearHistory(
        @AuthenticationPrincipal userDetails: UserDetails
    ): Mono<ResponseEntity<Void>> {
        val userId = UUID.fromString(userDetails.username)
        return historyService.clearHistory(userId)
            .thenReturn(ResponseEntity.noContent().build<Void>())
    }

    @DeleteMapping("/{id}")
    fun deleteHistoryItem(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: UUID
    ): Mono<ResponseEntity<Void>> {
        val userId = UUID.fromString(userDetails.username)
        return historyService.deleteHistoryItem(userId, id)
            .thenReturn(ResponseEntity.noContent().build<Void>())
    }
}