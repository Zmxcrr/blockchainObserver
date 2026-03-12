package com.blockchain.infrastructure.ethereum

import com.blockchain.domain.entity.Block
import com.blockchain.domain.entity.Transaction
import com.blockchain.domain.entity.TransactionStatus
import com.blockchain.domain.enum.Network
import com.blockchain.domain.port.BlockchainGateway
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant

@Component
class EthereumGateway(
    @Qualifier("ethereumWebClient") private val webClient: WebClient,
    @Value("\${app.etherscan.api-key:}") private val apiKey: String
) : BlockchainGateway {

    override fun getNetwork(): Network = Network.ETHEREUM

    override suspend fun getAddressBalance(address: String): BigDecimal {
        return try {
            val root = webClient.get()
                .uri { builder ->
                    builder.queryParam("module", "account")
                        .queryParam("action", "balance")
                        .queryParam("address", address)
                        .queryParam("tag", "latest")
                        .queryParam("apikey", apiKey)
                        .build()
                }
                .retrieve()
                .awaitBody<JsonNode>()

            val result = root.path("result")
            if (!result.isTextual) return BigDecimal.ZERO

            val wei = result.asText().toBigIntegerOrNull() ?: BigInteger.ZERO
            wei.toBigDecimal().divide(BigDecimal.TEN.pow(18))
        } catch (e: Exception) {
            BigDecimal.ZERO
        }
    }

    override fun getTransactionsByAddress(address: String, page: Int, size: Int): Flow<Transaction> = flow {
        val etherscanPage = page + 1
        try {
            val root = webClient.get()
                .uri { builder ->
                    builder.queryParam("module", "account")
                        .queryParam("action", "txlist")
                        .queryParam("address", address)
                        .queryParam("page", etherscanPage)
                        .queryParam("offset", size)
                        .queryParam("sort", "desc")
                        .queryParam("apikey", apiKey)
                        .build()
                }
                .retrieve()
                .awaitBody<JsonNode>()

            val status = root.path("status").asText("")
            val message = root.path("message").asText("")
            val resultNode = root.path("result")

            if (status == "0" && message.equals("No transactions found", ignoreCase = true)) {
                return@flow
            }
            if (!resultNode.isArray) return@flow

            resultNode.forEach { node ->
                runCatching {
                    val tx = parseEtherscanTxNode(node)
                    emit(tx.toDomain())
                }
            }
        } catch (e: Exception) {
        }
    }

    override suspend fun getTransactionByHash(hash: String): Transaction? = coroutineScope {
        try {
            val txResp = webClient.get()
                .uri { b ->
                    b.queryParam("module", "proxy")
                        .queryParam("action", "eth_getTransactionByHash")
                        .queryParam("txhash", hash)
                        .queryParam("apikey", apiKey)
                        .build()
                }
                .retrieve()
                .awaitBodyOrNull<EtherscanProxyTxResponse>()

            val tx = txResp?.result ?: return@coroutineScope null

            val blockNumberHex = tx.blockNumber ?: "0x0"

            val receiptDeferred = async {
                try {
                    webClient.get()
                        .uri { b ->
                            b.queryParam("module", "proxy")
                                .queryParam("action", "eth_getTransactionReceipt")
                                .queryParam("txhash", hash)
                                .queryParam("apikey", apiKey)
                                .build()
                        }
                        .retrieve()
                        .awaitBodyOrNull<EtherscanProxyReceiptResponse>()
                } catch (e: Exception) { null }
            }

            val blockDeferred = async {
                try {
                    webClient.get()
                        .uri { b ->
                            b.queryParam("module", "proxy")
                                .queryParam("action", "eth_getBlockByNumber")
                                .queryParam("tag", blockNumberHex)
                                .queryParam("boolean", "false")
                                .queryParam("apikey", apiKey)
                                .build()
                        }
                        .retrieve()
                        .awaitBodyOrNull<EtherscanProxyBlockResponse>()
                } catch (e: Exception) { null }
            }

            val receipt = receiptDeferred.await()?.result
            val block = blockDeferred.await()?.result

            val valueWei = hexToBigInt(tx.value)
            val gasPriceWei = hexToBigInt(tx.gasPrice)
            val gasUsedWei = hexToBigInt(receipt?.gasUsed)
            val feeWei = gasPriceWei.multiply(gasUsedWei)
            val status = when (receipt?.status?.lowercase()) {
                "0x1" -> TransactionStatus.SUCCESS
                "0x0" -> TransactionStatus.FAILED
                else -> TransactionStatus.PENDING
            }

            Transaction(
                hash = tx.hash ?: hash,
                network = Network.ETHEREUM,
                fromAddress = tx.from ?: "",
                toAddress = tx.to,
                amount = valueWei.toBigDecimal().divide(BigDecimal.TEN.pow(18)),
                fee = feeWei.toBigDecimal().divide(BigDecimal.TEN.pow(18)),
                blockNumber = hexToLong(tx.blockNumber),
                blockHash = tx.blockHash,
                timestamp = Instant.ofEpochSecond(hexToLong(block?.timestamp)),
                status = status,
                contractAddress = receipt?.contractAddress
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getBlock(numberOrHash: String): Block? {
        return try {
            val isHash = numberOrHash.startsWith("0x") && numberOrHash.length > 10

            val response = if (isHash) {
                webClient.get()
                    .uri { b ->
                        b.queryParam("module", "proxy")
                            .queryParam("action", "eth_getBlockByHash")
                            .queryParam("tag", numberOrHash)
                            .queryParam("boolean", "true")
                            .queryParam("apikey", apiKey)
                            .build()
                    }
                    .retrieve()
                    .awaitBodyOrNull<EtherscanProxyBlockResponse>()
            } else {
                val blockNum = numberOrHash.toLongOrNull() ?: return null
                val hex = "0x${blockNum.toString(16)}"
                webClient.get()
                    .uri { b ->
                        b.queryParam("module", "proxy")
                            .queryParam("action", "eth_getBlockByNumber")
                            .queryParam("tag", hex)
                            .queryParam("boolean", "true")
                            .queryParam("apikey", apiKey)
                            .build()
                    }
                    .retrieve()
                    .awaitBodyOrNull<EtherscanProxyBlockResponse>()
            }

            response?.result?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getLatestBlock(): Block? {
        return try {
            val latestNumResp = webClient.get()
                .uri { b ->
                    b.queryParam("module", "proxy")
                        .queryParam("action", "eth_blockNumber")
                        .queryParam("apikey", apiKey)
                        .build()
                }
                .retrieve()
                .awaitBodyOrNull<EtherscanProxyHexResult>()

            val latestHex = latestNumResp?.result ?: return null

            val blockResp = webClient.get()
                .uri { b ->
                    b.queryParam("module", "proxy")
                        .queryParam("action", "eth_getBlockByNumber")
                        .queryParam("tag", latestHex)
                        .queryParam("boolean", "true")
                        .queryParam("apikey", apiKey)
                        .build()
                }
                .retrieve()
                .awaitBodyOrNull<EtherscanProxyBlockResponse>()

            blockResp?.result?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    private fun parseEtherscanTxNode(node: com.fasterxml.jackson.databind.JsonNode): EtherscanTxDto {
        return EtherscanTxDto(
            blockNumber = node.path("blockNumber").asText("0"),
            timeStamp = node.path("timeStamp").asText("0"),
            hash = node.path("hash").asText(""),
            blockHash = node.path("blockHash").asText(""),
            from = node.path("from").asText(""),
            to = node.path("to").asText(""),
            value = node.path("value").asText("0"),
            gasPrice = node.path("gasPrice").asText("0"),
            gasUsed = node.path("gasUsed").asText("0"),
            isError = node.path("isError").asText("0"),
            contractAddress = node.path("contractAddress").asText("")
        )
    }

    private fun EtherscanTxDto.toDomain(): Transaction {
        val valueWei = value.toBigIntegerOrNull() ?: BigInteger.ZERO
        val gasUsedBi = gasUsed.toBigIntegerOrNull() ?: BigInteger.ZERO
        val gasPriceBi = gasPrice.toBigIntegerOrNull() ?: BigInteger.ZERO
        val feeWei = gasUsedBi.multiply(gasPriceBi)
        return Transaction(
            hash = hash,
            network = Network.ETHEREUM,
            fromAddress = from,
            toAddress = to.ifBlank { null },
            amount = valueWei.toBigDecimal().divide(BigDecimal.TEN.pow(18)),
            fee = feeWei.toBigDecimal().divide(BigDecimal.TEN.pow(18)),
            blockNumber = blockNumber.toLongOrNull() ?: 0L,
            blockHash = blockHash.ifBlank { null },
            timestamp = Instant.ofEpochSecond(timeStamp.toLongOrNull() ?: 0L),
            status = when (isError) {
                "0" -> TransactionStatus.SUCCESS
                "1" -> TransactionStatus.FAILED
                else -> TransactionStatus.PENDING
            },
            contractAddress = contractAddress.ifBlank { null }
        )
    }

    private fun EtherscanProxyBlock.toDomain(): Block = Block(
        hash = hash ?: "",
        number = hexToLong(number),
        network = Network.ETHEREUM,
        parentHash = parentHash,
        timestamp = Instant.ofEpochSecond(hexToLong(timestamp)),
        transactionCount = transactions?.size ?: 0
    )

    private fun hexToBigInt(hex: String?): BigInteger =
        hex?.removePrefix("0x")
            ?.takeIf { it.isNotBlank() }
            ?.let { BigInteger(it, 16) }
            ?: BigInteger.ZERO

    private fun hexToLong(hex: String?): Long = hexToBigInt(hex).toLong()
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class EtherscanTxDto(
    val blockNumber: String = "0",
    val timeStamp: String = "0",
    val hash: String = "",
    val blockHash: String = "",
    val from: String = "",
    val to: String = "",
    val value: String = "0",
    val gasPrice: String = "0",
    val gasUsed: String = "0",
    val isError: String = "0",
    val contractAddress: String = ""
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EtherscanProxyHexResult(
    val status: String? = null,
    val message: String? = null,
    val result: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EtherscanProxyTxResponse(
    val status: String? = null,
    val message: String? = null,
    val result: EtherscanProxyTx? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EtherscanProxyTx(
    val hash: String? = null,
    val from: String? = null,
    val to: String? = null,
    val value: String? = null,
    val gasPrice: String? = null,
    val blockNumber: String? = null,
    val blockHash: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EtherscanProxyReceiptResponse(
    val status: String? = null,
    val message: String? = null,
    val result: EtherscanProxyReceipt? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EtherscanProxyReceipt(
    val status: String? = null,
    val gasUsed: String? = null,
    val contractAddress: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EtherscanProxyBlockResponse(
    val status: String? = null,
    val message: String? = null,
    val result: EtherscanProxyBlock? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EtherscanProxyBlock(
    val hash: String? = null,
    val parentHash: String? = null,
    val number: String? = null,
    val timestamp: String? = null,
    @JsonProperty("transactions") val transactions: List<Any>? = emptyList()
)