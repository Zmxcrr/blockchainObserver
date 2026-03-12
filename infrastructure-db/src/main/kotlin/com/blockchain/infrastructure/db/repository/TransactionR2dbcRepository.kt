package com.blockchain.infrastructure.db.repository

import com.blockchain.infrastructure.db.entity.TransactionEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import kotlinx.coroutines.flow.Flow

interface TransactionR2dbcRepository : CoroutineCrudRepository<TransactionEntity, Long> {

    suspend fun findByHashAndNetwork(hash: String, network: String): TransactionEntity?

    @Query("""
        SELECT * FROM transactions
        WHERE network = :network AND (from_address = :address OR to_address = :address)
        ORDER BY timestamp DESC LIMIT :size OFFSET :offset
    """)
    fun findByAddressAndNetwork(address: String, network: String, size: Int, offset: Int): Flow<TransactionEntity>
}