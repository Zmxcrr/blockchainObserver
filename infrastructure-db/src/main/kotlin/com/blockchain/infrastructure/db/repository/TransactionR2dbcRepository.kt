package com.blockchain.infrastructure.db.repository

import com.blockchain.infrastructure.db.entity.TransactionEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface TransactionR2dbcRepository : ReactiveCrudRepository<TransactionEntity, Long> {

    fun findByHashAndNetwork(hash: String, network: String): Mono<TransactionEntity>

    @Query("""
        SELECT * FROM transactions
        WHERE network = :network AND (from_address = :address OR to_address = :address)
        ORDER BY timestamp DESC LIMIT :size OFFSET :offset
    """)
    fun findByAddressAndNetwork(address: String, network: String, size: Int, offset: Int): Flux<TransactionEntity>
}