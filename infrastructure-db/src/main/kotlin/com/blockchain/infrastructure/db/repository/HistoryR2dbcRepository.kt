package com.blockchain.infrastructure.db.repository

import com.blockchain.infrastructure.db.entity.SearchHistoryEntity
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface HistoryR2dbcRepository : CoroutineCrudRepository<SearchHistoryEntity, UUID> {

    @Query("SELECT * FROM search_history WHERE user_id = :userId ORDER BY created_at DESC LIMIT :size OFFSET :offset")
    fun findByUserIdPaged(userId: UUID, size: Int, offset: Int): Flow<SearchHistoryEntity>

    @Modifying
    @Query("DELETE FROM search_history WHERE user_id = :userId")
    suspend fun deleteByUserId(userId: UUID): Int
}