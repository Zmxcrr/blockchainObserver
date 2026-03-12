package com.blockchain.application.dto

import com.blockchain.domain.entity.TransactionStatus
import com.blockchain.domain.enum.Network

data class TransactionResponse(
    val hash: String,
    val network: Network,
    val fromAddress: String,
    val toAddress: String?,
    val amount: String,
    val fee: String,
    val blockNumber: Long,
    val timestamp: String,
    val status: TransactionStatus
)

data class BlockResponse(
    val hash: String,
    val number: Long,
    val network: Network,
    val timestamp: String,
    val transactionCount: Int
)

data class AddressResponse(
    val address: String,
    val network: Network,
    val balance: String,
    val currency: String,
    val transactionCount: Int,
    val transactions: List<TransactionResponse>
)

data class PagedResponse<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val total: Int
)