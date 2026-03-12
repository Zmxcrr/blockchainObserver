package com.blockchain.domain.port

import com.blockchain.domain.entity.Transaction
import com.blockchain.domain.enum.Network
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface TransactionRepositoryPort {
    fun save(transaction: Transaction): Mono<Transaction>
    fun findByHash(hash: String, network: Network): Mono<Transaction>
    fun findByAddress(address: String, network: Network, page: Int, size: Int): Flux<Transaction>

    fun findExistingHashes(hashes: List<String>, network: Network): Mono<Set<String>>
    fun saveAllIgnoreConflicts(transactions: List<Transaction>): Mono<Void>

    fun saveAll(transactions: List<Transaction>): Flux<Transaction>
}