package com.blockchain.application.service

import com.blockchain.application.dto.AddressResponse
import com.blockchain.application.dto.BlockResponse
import com.blockchain.application.dto.TransactionResponse
import com.blockchain.domain.entity.Block
import com.blockchain.domain.entity.Transaction
import com.blockchain.domain.enum.Network
import com.blockchain.domain.enum.SearchType
import com.blockchain.domain.port.BlockchainGateway
import com.blockchain.domain.port.TransactionRepositoryPort
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class BlockchainService(
    private val gateways: List<BlockchainGateway>,
    private val transactionRepositoryPort: TransactionRepositoryPort,
    private val historyService: HistoryService
) {
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun gatewayFor(network: Network): BlockchainGateway =
        gateways.firstOrNull { it.getNetwork() == network }
            ?: throw IllegalArgumentException("No gateway registered for network: $network")

    private fun recordHistoryAsync(userId: UUID?, query: String, network: Network, type: SearchType) {
        userId?.let {
            backgroundScope.launch {
                try {
                    historyService.recordSearch(it, query, network, type)
                } catch (e: Exception) {
                    println("Failed to record history: ${e.message}")
                }
            }
        }
    }

    fun getAddressTransactions(
        userId: UUID?,
        address: String,
        network: Network,
        page: Int,
        size: Int
    ): Flow<TransactionResponse> = flow {
        val safeSize = size.coerceAtMost(100)
        recordHistoryAsync(userId, address, network, SearchType.ADDRESS)

        val fetchedTransactions = try {
            
            gatewayFor(network).getTransactionsByAddress(address, page, safeSize).toList()
        } catch (error: Exception) {
            println("Failed to fetch from API, fallback to DB: ${error.message}")
            null
        }

        if (fetchedTransactions != null) {
            persistWithoutDuplicates(fetchedTransactions, network)
            
            for (tx in fetchedTransactions) {
                emit(tx.toResponse())
            }
        } else {
            
            
            val dbFlow = transactionRepositoryPort.findByAddress(address, network, page, safeSize)
                .map { it.toResponse() } 
            emitAll(dbFlow)
        }
    }

    suspend fun getAddress(
        userId: UUID?,
        address: String,
        network: Network,
        page: Int,
        size: Int
    ): AddressResponse = coroutineScope {
        val gateway = gatewayFor(network)
        val safeSize = size.coerceAtMost(100)

        recordHistoryAsync(userId, address, network, SearchType.ADDRESS)

        
        val balanceDeferred = async { gateway.getAddressBalance(address) }

        val transactionsDeferred = async {
            try {
                val fetched = gateway.getTransactionsByAddress(address, page, safeSize).toList()
                persistWithoutDuplicates(fetched, network)
                fetched
            } catch (error: Exception) {
                println("Failed to fetch from API, fallback to DB: ${error.message}")
                transactionRepositoryPort.findByAddress(address, network, page, safeSize).toList()
            }
        }

        
        val balance = balanceDeferred.await()
        val transactions = transactionsDeferred.await()

        
        
        val transactionResponses = transactions.map { it.toResponse() }

        AddressResponse(
            address = address,
            network = network,
            
            balance = balance.toPlainString(),
            currency = if (network == Network.TRON) "TRX" else "ETH",
            transactionCount = transactionResponses.size, 
            transactions = transactionResponses
        )
    }

    suspend fun getTransactionByHash(hash: String, network: Network, userId: UUID? = null): TransactionResponse {
        recordHistoryAsync(userId, hash, network, SearchType.TX)

        var tx = transactionRepositoryPort.findByHash(hash, network)

        if (tx == null) {
            tx = gatewayFor(network).getTransactionByHash(hash)
                ?: throw NoSuchElementException("Transaction not found: $hash")

            transactionRepositoryPort.save(tx)
        }

        return tx.toResponse() 
    }

    suspend fun getBlock(numberOrHash: String, network: Network, userId: UUID? = null): BlockResponse {
        recordHistoryAsync(userId, numberOrHash, network, SearchType.BLOCK)

        val block = gatewayFor(network).getBlock(numberOrHash)
            ?: throw NoSuchElementException("Block not found: $numberOrHash")

        return block.toResponse() 
    }

    suspend fun getLatestBlock(network: Network, userId: UUID? = null): BlockResponse {
        recordHistoryAsync(userId, "latest", network, SearchType.BLOCK)

        val block = gatewayFor(network).getLatestBlock()
            ?: throw NoSuchElementException("No latest block available")

        return block.toResponse()
    }

    private suspend fun persistWithoutDuplicates(fetched: List<Transaction>, network: Network) {
        if (fetched.isEmpty()) return

        val existingHashes = transactionRepositoryPort.findExistingHashes(
            hashes = fetched.map { it.hash }.distinct(),
            network = network
        )

        val toSave = fetched.filterNot { it.hash in existingHashes }
        if (toSave.isNotEmpty()) {
            transactionRepositoryPort.saveAllIgnoreConflicts(toSave)
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