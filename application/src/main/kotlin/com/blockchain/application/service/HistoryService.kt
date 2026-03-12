package com.blockchain.application.service

import com.blockchain.application.dto.SearchHistoryResponse
import com.blockchain.domain.entity.SearchHistory
import com.blockchain.domain.enum.Network
import com.blockchain.domain.enum.SearchType
import com.blockchain.domain.port.HistoryRepositoryPort
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class HistoryService(
    private val historyRepositoryPort: HistoryRepositoryPort
) {
    fun recordSearch(userId: UUID, query: String, network: Network, searchType: SearchType): Mono<SearchHistory> {
        val history = SearchHistory(
            id = UUID.randomUUID(),
            userId = userId,
            query = query,
            network = network,
            searchType = searchType
        )
        return historyRepositoryPort.save(history)
    }

    fun getHistory(userId: UUID, page: Int = 0, size: Int = 20): Flux<SearchHistoryResponse> {
        return historyRepositoryPort.findByUserId(userId, page, size).map { it.toResponse() }
    }

    fun clearHistory(userId: UUID): Mono<Void> {
        return historyRepositoryPort.deleteByUserId(userId)
    }

    fun deleteHistoryItem(userId: UUID, id: UUID): Mono<Void> {
        return historyRepositoryPort.findByUserId(userId, 0, Int.MAX_VALUE)
            .filter { it.id == id }
            .next()
            .switchIfEmpty(Mono.error(IllegalArgumentException("History item not found for this user")))
            .flatMap { historyRepositoryPort.deleteById(id) }
    }

    private fun SearchHistory.toResponse() = SearchHistoryResponse(
        id = id.toString(),
        query = query,
        network = network,
        searchType = searchType,
        createdAt = createdAt.toString()
    )
}
