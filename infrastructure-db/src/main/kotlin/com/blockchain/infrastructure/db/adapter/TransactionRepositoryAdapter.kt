package com.blockchain.infrastructure.db.adapter

import com.blockchain.domain.entity.Transaction
import com.blockchain.domain.enum.Network
import com.blockchain.domain.port.TransactionRepositoryPort
import com.blockchain.infrastructure.db.mapper.toDomain
import com.blockchain.infrastructure.db.mapper.toEntity
import com.blockchain.infrastructure.db.repository.TransactionR2dbcRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
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

    override suspend fun save(transaction: Transaction): Transaction =
        transactionR2dbcRepository.save(transaction.toEntity()).toDomain()

    override suspend fun findByHash(hash: String, network: Network): Transaction? =
        transactionR2dbcRepository.findByHashAndNetwork(hash, network.name)?.toDomain()

    override fun findByAddress(address: String, network: Network, page: Int, size: Int): Flow<Transaction> {
        val offset = page * size
        return transactionR2dbcRepository.findByAddressAndNetwork(address, network.name, size, offset)
            .map { it.toDomain() }
    }

    override suspend fun findExistingHashes(hashes: List<String>, network: Network): Set<String> {
        if (hashes.isEmpty()) return emptySet()

        return databaseClient.sql(
            "SELECT hash FROM transactions WHERE network = $1 AND hash = ANY($2)"
        )
            .bind(0, network.name)
            .bind(1, hashes.distinct().toTypedArray())
            .map { row, _ -> row.get("hash", String::class.java)!! }
            .all()
            .collectList()
            .awaitSingle()
            .toSet()
    }

    override suspend fun saveAllIgnoreConflicts(transactions: List<Transaction>) {
        if (transactions.isEmpty()) return

        val sql = """
            INSERT INTO transactions (amount, block_hash, block_number, contract_address, fee,
                from_address, hash, network, status, timestamp, to_address)
            VALUES (:amount, :blockHash, :blockNumber, :contractAddress, :fee,
                :fromAddress, :hash, :network, :status, :timestamp, :toAddress)
            ON CONFLICT (hash, network) DO NOTHING
        """.trimIndent()

        for (tx in transactions) {
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
                .awaitSingleOrNull()
        }
    }

    override fun saveAll(transactions: List<Transaction>): Flow<Transaction> =
        transactionR2dbcRepository.saveAll(transactions.map { it.toEntity() })
            .map { it.toDomain() }
}