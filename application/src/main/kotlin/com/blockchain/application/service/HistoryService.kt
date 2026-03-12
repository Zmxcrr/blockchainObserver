package com.blockchain.application.service

import com.blockchain.application.dto.SearchHistoryResponse
import com.blockchain.domain.entity.SearchHistory
import com.blockchain.domain.enum.Network
import com.blockchain.domain.enum.SearchType
import com.blockchain.domain.port.HistoryRepositoryPort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.filter
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class HistoryService(
    private val historyRepositoryPort: HistoryRepositoryPort
) {
    suspend fun recordSearch(userId: UUID, query: String, network: Network, searchType: SearchType): SearchHistory {
        val history = SearchHistory(
            id = UUID.randomUUID(),
            userId = userId,
            query = query,
            network = network,
            searchType = searchType
        )
        return historyRepositoryPort.save(history)
    }

    fun getHistory(userId: UUID, page: Int = 0, size: Int = 20): Flow<SearchHistoryResponse> {
        return historyRepositoryPort.findByUserId(userId, page, size).map { it.toResponse() }
    }

    suspend fun clearHistory(userId: UUID) {
        historyRepositoryPort.deleteByUserId(userId)
    }

    suspend fun deleteHistoryItem(userId: UUID, id: UUID) {
        
        val item = historyRepositoryPort.findByUserId(userId, 0, Int.MAX_VALUE)
            .filter { it.id == id }
            .firstOrNull()
            ?: throw IllegalArgumentException("History item not found for this user")

        historyRepositoryPort.deleteById(item.id)
    }

    private fun SearchHistory.toResponse() = SearchHistoryResponse(
        id = id.toString(),
        query = query,
        network = network,
        searchType = searchType,
        createdAt = createdAt.toString()
    )
}