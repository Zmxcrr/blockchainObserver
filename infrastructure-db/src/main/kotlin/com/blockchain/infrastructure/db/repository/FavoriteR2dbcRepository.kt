package com.blockchain.infrastructure.db.repository

import com.blockchain.infrastructure.db.entity.FavoriteAddressEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FavoriteR2dbcRepository : CoroutineCrudRepository<FavoriteAddressEntity, UUID> {
    fun findByUserId(userId: UUID): Flow<FavoriteAddressEntity>

    suspend fun findByIdAndUserId(id: UUID, userId: UUID): FavoriteAddressEntity?

    @Query("SELECT * FROM favorite_addresses WHERE user_id = :userId AND address = :address AND network = :network LIMIT 1")
    suspend fun findByUserIdAndAddressAndNetwork(userId: UUID, address: String, network: String): FavoriteAddressEntity?

    @Query("SELECT COUNT(*) > 0 FROM favorite_addresses WHERE user_id = :userId AND address = :address AND network = :network")
    suspend fun existsByUserIdAndAddressAndNetwork(userId: UUID, address: String, network: String): Boolean
}