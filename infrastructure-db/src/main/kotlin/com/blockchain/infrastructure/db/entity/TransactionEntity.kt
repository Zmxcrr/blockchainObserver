package com.blockchain.infrastructure.db.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("transactions")
data class TransactionEntity(
    @Id val id: Long? = null,
    val hash: String,
    val network: String,
    @Column("from_address") val fromAddress: String,
    @Column("to_address") val toAddress: String?,
    val amount: BigDecimal,
    val fee: BigDecimal,
    @Column("block_number") val blockNumber: Long,
    @Column("block_hash") val blockHash: String?,
    val timestamp: LocalDateTime,
    val status: String,
    @Column("contract_address") val contractAddress: String?
)