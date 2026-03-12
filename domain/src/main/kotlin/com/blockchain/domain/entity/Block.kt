package com.blockchain.domain.entity

import com.blockchain.domain.enum.Network

data class Block(
    val hash: String,
    val number: Long,
    val network: Network,
    val parentHash: String?,
    val timestamp: java.time.Instant,
    val transactionCount: Int,
    val witnessAddress: String? = null
)