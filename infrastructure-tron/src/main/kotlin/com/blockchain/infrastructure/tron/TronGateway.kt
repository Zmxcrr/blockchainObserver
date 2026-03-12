package com.blockchain.infrastructure.tron

import com.blockchain.domain.entity.Block
import com.blockchain.domain.entity.Transaction
import com.blockchain.domain.entity.TransactionStatus
import com.blockchain.domain.enum.Network
import com.blockchain.domain.port.BlockchainGateway
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.Instant

@Component
class TronGateway(
    @Qualifier("tronWebClient") private val webClient: WebClient
) : BlockchainGateway {

    override fun getNetwork(): Network = Network.TRON

    override fun getTransactionsByAddress(address: String, page: Int, size: Int): Flux<Transaction> {
        return webClient.get()
            .uri("/v1/accounts/$address/transactions?limit=$size&start=${page * size}")
            .retrieve()
            .bodyToMono<TronTransactionListResponse>()
            .flatMapIterable { it.data.map { tx -> tx.toDomain() } }
            .onErrorResume { Flux.empty() }
    }

    override fun getAddressBalance(address: String): Mono<BigDecimal> {
        return webClient.get()
            .uri("/v1/accounts/$address")
            .retrieve()
            .bodyToMono<TronAccountResponse>()
            .map { response ->
                val sun = response.data.firstOrNull()?.balance ?: 0L
                sun.toBigDecimal().divide(BigDecimal("1000000"))
            }
            .onErrorReturn(BigDecimal.ZERO)
    }

    override fun getTransactionByHash(hash: String): Mono<Transaction> {
        return webClient.post()
            .uri("/wallet/gettransactionbyid")
            .bodyValue(mapOf("value" to hash))
            .retrieve()
            .bodyToMono<TronTransactionResponse>()
            .filter { it.txId.isNotBlank() }
            .map { it.toDomain() }
            .onErrorResume { Mono.empty() }
    }

    override fun getBlock(numberOrHash: String): Mono<Block> {
        val num = numberOrHash.toLongOrNull()
        return if (num != null) {
            webClient.post()
                .uri("/wallet/getblockbynum")
                .bodyValue(mapOf("num" to num, "visible" to true))
                .retrieve()
                .bodyToMono<TronBlockResponse>()
                .filter { it.block_header != null || !it.blockId.isNullOrBlank() }
                .map { it.toDomain(hashFallback = numberOrHash) }
        } else {
            webClient.post()
                .uri("/wallet/getblockbyid")
                .bodyValue(mapOf("value" to numberOrHash, "visible" to true))
                .retrieve()
                .bodyToMono<TronBlockResponse>()
                .filter { it.block_header != null || !it.blockId.isNullOrBlank() }
                .map { it.toDomain(hashFallback = numberOrHash) }
        }
    }

    override fun getLatestBlock(): Mono<Block> {
        return webClient.post()
            .uri("/wallet/getnowblock")
            .bodyValue(mapOf("visible" to true))
            .retrieve()
            .bodyToMono<TronBlockResponse>()
            .filter { it.block_header != null || !it.blockId.isNullOrBlank() }
            .map { it.toDomain() }
    }
}


@JsonIgnoreProperties(ignoreUnknown = true)
data class TronTransactionListResponse(
    val data: List<TronTransactionResponse> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TronTransactionResponse(
    @JsonProperty("txID") val txId: String = "",
    val raw_data: TronRawData = TronRawData(),
    val ret: List<TronRet> = emptyList()
) {
    fun toDomain(): Transaction {
        val contract = raw_data.contract.firstOrNull()
        val value = contract?.parameter?.value
        return Transaction(
            hash = txId,
            network = Network.TRON,
            fromAddress = value?.ownerAddress ?: "",
            toAddress = value?.toAddress,
            amount = (value?.amount ?: 0L).toBigDecimal().divide(BigDecimal("1000000")),
            fee = BigDecimal.ZERO,
            blockNumber = raw_data.refBlockNum ?: 0L,
            blockHash = null,
            timestamp = Instant.ofEpochMilli(raw_data.timestamp ?: 0L),
            status = if (ret.firstOrNull()?.contractRet == "SUCCESS") TransactionStatus.SUCCESS
            else TransactionStatus.FAILED
        )
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class TronAccountResponse(
    val data: List<TronAccountData> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TronAccountData(
    val balance: Long? = 0L
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TronRawData(
    val contract: List<TronContract> = emptyList(),
    val timestamp: Long? = null,
    @JsonProperty("ref_block_num") val refBlockNum: Long? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TronContract(
    val parameter: TronParameter = TronParameter()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TronParameter(
    val value: TronContractValue? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TronContractValue(
    @JsonProperty("owner_address") val ownerAddress: String? = null,
    @JsonProperty("to_address") val toAddress: String? = null,
    val amount: Long? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TronRet(
    @JsonProperty("contractRet") val contractRet: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TronBlockResponse(
    @JsonProperty("blockID") val blockId: String? = null,
    val block_header: TronBlockHeader? = null,
    val transactions: List<TronTransactionResponse>? = null
) {
    fun toDomain(hashFallback: String = ""): Block = Block(
        hash = blockId ?: (if (hashFallback.toLongOrNull() == null) hashFallback else "<unknown>"),
        number = block_header?.raw_data?.number ?: 0L,
        network = Network.TRON,
        parentHash = block_header?.raw_data?.parentHash,
        timestamp = Instant.ofEpochMilli(block_header?.raw_data?.timestamp ?: 0L),
        transactionCount = transactions?.size ?: 0,
        witnessAddress = block_header?.raw_data?.witnessAddress
    )
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class TronBlockHeader(
    val raw_data: TronBlockHeaderData = TronBlockHeaderData()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TronBlockHeaderData(
    val number: Long? = null,
    val timestamp: Long? = null,
    @JsonProperty("parentHash") val parentHash: String? = null,
    @JsonProperty("witness_address") val witnessAddress: String? = null
)