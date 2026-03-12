package com.blockchain.domain.entity

import com.blockchain.domain.enum.Network
import java.math.BigDecimal

data class Transaction(
    val hash: String,
    val network: Network,
    val fromAddress: String,
    val toAddress: String?,
    val amount: BigDecimal,
    val fee: BigDecimal,
    val blockNumber: Long,
    val blockHash: String?,
    val timestamp: java.time.Instant,
    val status: TransactionStatus,
    val contractAddress: String? = null
)

enum class TransactionStatus { SUCCESS, FAILED, PENDING }