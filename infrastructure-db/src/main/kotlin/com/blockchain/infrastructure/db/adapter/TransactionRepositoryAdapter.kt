package com.blockchain.infrastructure.db.adapter

import com.blockchain.domain.entity.Transaction
import com.blockchain.domain.enum.Network
import com.blockchain.domain.port.TransactionRepositoryPort
import com.blockchain.infrastructure.db.mapper.toDomain
import com.blockchain.infrastructure.db.mapper.toEntity
import com.blockchain.infrastructure.db.repository.TransactionR2dbcRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.ZoneOffset

private fun <T : Any> DatabaseClient.GenericExecuteSpec.bindNullable(
    name: String,
    value: T?,
    type: Class<T>
): DatabaseClient.GenericExecuteSpec =
    if (value != null) bind(name, value) else bindNull(name, type)

@Component
class TransactionRepositoryAdapter(
    private val transactionR2dbcRepository: TransactionR2dbcRepository,
    private val databaseClient: DatabaseClient
) : TransactionRepositoryPort {

    override fun save(transaction: Transaction): Mono<Transaction> =
        transactionR2dbcRepository.save(transaction.toEntity()).map { it.toDomain() }

    override fun findByHash(hash: String, network: Network): Mono<Transaction> =
        transactionR2dbcRepository.findByHashAndNetwork(hash, network.name).map { it.toDomain() }

    override fun findByAddress(address: String, network: Network, page: Int, size: Int): Flux<Transaction> {
        val offset = page * size
        return transactionR2dbcRepository.findByAddressAndNetwork(address, network.name, size, offset)
            .map { it.toDomain() }
    }

    override fun findExistingHashes(hashes: List<String>, network: Network): Mono<Set<String>> {
        if (hashes.isEmpty()) return Mono.just(emptySet())

        return databaseClient.sql(
            "SELECT hash FROM transactions WHERE network = $1 AND hash = ANY($2)"
        )
            .bind(0, network.name)
            .bind(1, hashes.distinct().toTypedArray())
            .map { row, _ -> row.get("hash", String::class.java)!! }
            .all()
            .collectList()
            .map { it.toSet() }
    }

    override fun saveAllIgnoreConflicts(transactions: List<Transaction>): Mono<Void> {
        if (transactions.isEmpty()) return Mono.empty()
        val sql = """
            INSERT INTO transactions (amount, block_hash, block_number, contract_address, fee,
                from_address, hash, network, status, timestamp, to_address)
            VALUES (:amount, :blockHash, :blockNumber, :contractAddress, :fee,
                :fromAddress, :hash, :network, :status, :timestamp, :toAddress)
            ON CONFLICT (hash, network) DO NOTHING
        """.trimIndent()

        return Flux.fromIterable(transactions)
            .concatMap { tx ->
                val ldt = LocalDateTime.ofInstant(tx.timestamp, ZoneOffset.UTC)
                databaseClient.sql(sql)
                    .bind("amount", tx.amount)
                    .bind("blockNumber", tx.blockNumber)
                    .bind("fee", tx.fee)
                    .bind("fromAddress", tx.fromAddress)
                    .bind("hash", tx.hash)
                    .bind("network", tx.network.name)
                    .bind("status", tx.status.name)
                    .bind("timestamp", ldt)
                    .bindNullable("blockHash", tx.blockHash, String::class.java)
                    .bindNullable("contractAddress", tx.contractAddress, String::class.java)
                    .bindNullable("toAddress", tx.toAddress, String::class.java)
                    .then()
            }
            .then()
    }

    override fun saveAll(transactions: List<Transaction>): Flux<Transaction> =
        transactionR2dbcRepository.saveAll(transactions.map { it.toEntity() }).map { it.toDomain() }
}