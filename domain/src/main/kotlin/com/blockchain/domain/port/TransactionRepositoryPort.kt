package com.blockchain.domain.port

import com.blockchain.domain.entity.Transaction
import com.blockchain.domain.enum.Network
import kotlinx.coroutines.flow.Flow

interface TransactionRepositoryPort {
    suspend fun save(transaction: Transaction): Transaction
    suspend fun findByHash(hash: String, network: Network): Transaction?
    fun findByAddress(address: String, network: Network, page: Int, size: Int): Flow<Transaction>

    suspend fun findExistingHashes(hashes: List<String>, network: Network): Set<String>
    suspend fun saveAllIgnoreConflicts(transactions: List<Transaction>)

    fun saveAll(transactions: List<Transaction>): Flow<Transaction>
}