package com.blockchain.domain.port

import com.blockchain.domain.entity.Block
import com.blockchain.domain.entity.Transaction
import com.blockchain.domain.enum.Network
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal

interface BlockchainGateway {
    fun getTransactionsByAddress(
        address: String,
        page: Int,
        size: Int
    ): Flux<Transaction>

    fun getAddressBalance(address: String): Mono<BigDecimal>

    fun getTransactionByHash(hash: String): Mono<Transaction>

    fun getBlock(numberOrHash: String): Mono<Block>

    fun getLatestBlock(): Mono<Block>

    fun getNetwork(): Network
}