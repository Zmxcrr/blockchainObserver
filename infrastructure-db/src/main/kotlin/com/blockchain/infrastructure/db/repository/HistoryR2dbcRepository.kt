package com.blockchain.infrastructure.db.repository

import com.blockchain.infrastructure.db.entity.SearchHistoryEntity
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
interface HistoryR2dbcRepository : ReactiveCrudRepository<SearchHistoryEntity, UUID> {

    @Query("SELECT * FROM search_history WHERE user_id = :userId ORDER BY created_at DESC LIMIT :size OFFSET :offset")
    fun findByUserIdPaged(userId: UUID, size: Int, offset: Int): Flux<SearchHistoryEntity>

    @Modifying
    @Query("DELETE FROM search_history WHERE user_id = :userId")
    fun deleteByUserId(userId: UUID): Mono<Int>
}