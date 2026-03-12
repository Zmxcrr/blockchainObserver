package com.blockchain.domain.port

import com.blockchain.domain.entity.SearchHistory
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface HistoryRepositoryPort {
    suspend fun save(history: SearchHistory): SearchHistory
    fun findByUserId(userId: UUID, page: Int, size: Int): Flow<SearchHistory>
    suspend fun deleteByUserId(userId: UUID)
    suspend fun deleteById(id: UUID)
}