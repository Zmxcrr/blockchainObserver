package com.blockchain.domain.port

import com.blockchain.domain.entity.SearchHistory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface HistoryRepositoryPort {
    fun save(history: SearchHistory): Mono<SearchHistory>
    fun findByUserId(userId: UUID, page: Int, size: Int): Flux<SearchHistory>
    fun deleteByUserId(userId: UUID): Mono<Void>
    fun deleteById(id: UUID): Mono<Void>
}