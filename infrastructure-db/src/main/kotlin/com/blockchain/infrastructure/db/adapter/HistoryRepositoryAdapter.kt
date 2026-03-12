package com.blockchain.infrastructure.db.adapter

import com.blockchain.domain.entity.SearchHistory
import com.blockchain.domain.port.HistoryRepositoryPort
import com.blockchain.infrastructure.db.mapper.toDomain
import com.blockchain.infrastructure.db.mapper.toEntity
import com.blockchain.infrastructure.db.repository.HistoryR2dbcRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class HistoryRepositoryAdapter(
    private val historyR2dbcRepository: HistoryR2dbcRepository
) : HistoryRepositoryPort {

    override suspend fun save(history: SearchHistory): SearchHistory {
        return historyR2dbcRepository.save(history.toEntity(isNew = true)).toDomain()
    }

    override fun findByUserId(userId: UUID, page: Int, size: Int): Flow<SearchHistory> {
        val offset = page * size
        return historyR2dbcRepository.findByUserIdPaged(userId, size, offset).map { it.toDomain() }
    }

    override suspend fun deleteByUserId(userId: UUID) {
        historyR2dbcRepository.deleteByUserId(userId)
    }

    override suspend fun deleteById(id: UUID) {
        historyR2dbcRepository.deleteById(id)
    }
}