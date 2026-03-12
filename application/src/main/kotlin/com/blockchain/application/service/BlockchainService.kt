package com.blockchain.application.service

import com.blockchain.application.dto.AddressResponse
import com.blockchain.application.dto.BlockResponse
import com.blockchain.application.dto.PagedResponse
import com.blockchain.application.dto.TransactionResponse
import com.blockchain.domain.entity.Block
import com.blockchain.domain.entity.Transaction
import com.blockchain.domain.enum.Network
import com.blockchain.domain.enum.SearchType
import com.blockchain.domain.port.BlockchainGateway
import com.blockchain.domain.port.TransactionRepositoryPort
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class BlockchainService(
    private val gateways: List<BlockchainGateway>,
    private val transactionRepositoryPort: TransactionRepositoryPort,
    private val historyService: HistoryService
) {
    private fun gatewayFor(network: Network): BlockchainGateway =
        gateways.firstOrNull { it.getNetwork() == network }
            ?: throw IllegalArgumentException("No gateway registered for network: $network")

    private fun recordHistoryAsync(userId: UUID?, query: String, network: Network, type: SearchType) {
        userId?.let {
            historyService.recordSearch(it, query, network, type)
                .subscribe(
                    {},
                    { error -> println("Failed to record history: ${error.message}") }
                )
        }
    }

    fun getAddressTransactions(
        userId: UUID?,
        address: String,
        network: Network,
        page: Int,
        size: Int
    ): Flux<TransactionResponse> {
        val safeSize = size.coerceAtMost(100)

        recordHistoryAsync(userId, address, network, SearchType.ADDRESS)

        return transactionRepositoryPort.findByAddress(address, network, page, safeSize)
            .switchIfEmpty(
                gatewayFor(network).getTransactionsByAddress(address, page, safeSize)
                    .collectList()
                    .flatMapMany { fetched ->
                        persistWithoutDuplicates(fetched, network)
                            .thenMany(Flux.fromIterable(fetched))
                    }
            )
            .map { it.toResponse() }
    }

    fun getAddress(
        userId: UUID?,
        address: String,
        network: Network,
        page: Int,
        size: Int
    ): Mono<AddressResponse> {
        val gateway = gatewayFor(network)
        val safeSize = size.coerceAtMost(100)

        recordHistoryAsync(userId, address, network, SearchType.ADDRESS)

        val balance = gateway.getAddressBalance(address)

        val transactionsMono = transactionRepositoryPort.findByAddress(address, network, page, safeSize)
            .switchIfEmpty(
                gateway.getTransactionsByAddress(address, page, safeSize)
                    .collectList()
                    .flatMapMany { fetched ->
                        persistWithoutDuplicates(fetched, network).thenMany(Flux.fromIterable(fetched))
                    }
            )
            .map { it.toResponse() }
            .collectList()

        return Mono.zip(balance, transactionsMono).map { tuple ->
            AddressResponse(
                address = address,
                network = network,
                balance = tuple.t1.toPlainString(),
                currency = if (network == Network.TRON) "TRX" else "ETH",
                transactionCount = tuple.t2.size,
                transactions = tuple.t2
            )
        }
    }

    fun getTransactionByHash(hash: String, network: Network, userId: UUID? = null): Mono<TransactionResponse> {

        recordHistoryAsync(userId, hash, network, SearchType.TX)

        return transactionRepositoryPort.findByHash(hash, network)
            .switchIfEmpty(
                gatewayFor(network).getTransactionByHash(hash)
                    .switchIfEmpty(Mono.error(NoSuchElementException("Transaction not found: $hash")))
                    .flatMap { tx -> transactionRepositoryPort.save(tx) }
            )
            .map { it.toResponse() }
    }

    fun getBlock(numberOrHash: String, network: Network, userId: UUID? = null): Mono<BlockResponse> {

        recordHistoryAsync(userId, numberOrHash, network, SearchType.BLOCK)

        return gatewayFor(network).getBlock(numberOrHash)
            .switchIfEmpty(Mono.error(NoSuchElementException("Block not found: $numberOrHash")))
            .map { it.toResponse() }
    }

    fun getLatestBlock(network: Network, userId: UUID? = null): Mono<BlockResponse> {

        recordHistoryAsync(userId, "latest", network, SearchType.BLOCK)

        return gatewayFor(network).getLatestBlock()
            .switchIfEmpty(Mono.error(NoSuchElementException("No latest block available")))
            .map { it.toResponse() }
    }

    private fun persistWithoutDuplicates(fetched: List<Transaction>, network: Network): Mono<Void> {
        if (fetched.isEmpty()) return Mono.empty()
        return transactionRepositoryPort.findExistingHashes(
            hashes = fetched.map { it.hash }.distinct(),
            network = network
        ).flatMap { existing ->
            val toSave = fetched.filterNot { it.hash in existing }
            if (toSave.isNotEmpty()) transactionRepositoryPort.saveAllIgnoreConflicts(toSave)
            else Mono.empty()
        }
    }

    private fun Transaction.toResponse() = TransactionResponse(
        hash = hash,
        network = network,
        fromAddress = fromAddress,
        toAddress = toAddress,
        amount = amount.toPlainString(),
        fee = fee.toPlainString(),
        blockNumber = blockNumber,
        timestamp = timestamp.toString(),
        status = status
    )

    private fun Block.toResponse() = BlockResponse(
        hash = hash,
        number = number,
        network = network,
        timestamp = timestamp.toString(),
        transactionCount = transactionCount
    )
}