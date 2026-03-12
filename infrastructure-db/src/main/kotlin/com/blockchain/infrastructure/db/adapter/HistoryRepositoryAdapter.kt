package com.blockchain.infrastructure.db.adapter

import com.blockchain.domain.entity.SearchHistory
import com.blockchain.domain.port.HistoryRepositoryPort
import com.blockchain.infrastructure.db.mapper.toDomain
import com.blockchain.infrastructure.db.mapper.toEntity
import com.blockchain.infrastructure.db.repository.HistoryR2dbcRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class HistoryRepositoryAdapter(
    private val historyR2dbcRepository: HistoryR2dbcRepository
) : HistoryRepositoryPort {

    override fun save(history: SearchHistory): Mono<SearchHistory> {
        return historyR2dbcRepository.save(history.toEntity(isNew = true))
            .map { it.toDomain() }
    }

    override fun findByUserId(userId: UUID, page: Int, size: Int): Flux<SearchHistory> {
        val offset = page * size
        return historyR2dbcRepository.findByUserIdPaged(userId, size, offset).map { it.toDomain() }
    }

    override fun deleteByUserId(userId: UUID): Mono<Void> =
        historyR2dbcRepository.deleteByUserId(userId).then()

    override fun deleteById(id: UUID): Mono<Void> =
        historyR2dbcRepository.deleteById(id)
}