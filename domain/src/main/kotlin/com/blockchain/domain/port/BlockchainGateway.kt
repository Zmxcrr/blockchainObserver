package com.blockchain.domain.port

import com.blockchain.domain.entity.Block
import com.blockchain.domain.entity.Transaction
import com.blockchain.domain.enum.Network
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface BlockchainGateway {
    
    fun getTransactionsByAddress(
        address: String,
        page: Int,
        size: Int
    ): Flow<Transaction>

    
    suspend fun getAddressBalance(address: String): BigDecimal

    
    suspend fun getTransactionByHash(hash: String): Transaction?

    suspend fun getBlock(numberOrHash: String): Block?

    suspend fun getLatestBlock(): Block?

    fun getNetwork(): Network
}