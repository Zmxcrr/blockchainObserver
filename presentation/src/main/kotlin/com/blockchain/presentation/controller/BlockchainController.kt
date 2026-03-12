package com.blockchain.presentation.controller

import com.blockchain.application.dto.AddressResponse
import com.blockchain.application.dto.BlockResponse
import com.blockchain.application.dto.TransactionResponse
import com.blockchain.application.service.BlockchainService
import com.blockchain.application.usecase.GetAddressTransactionsUseCase
import com.blockchain.domain.enum.Network
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@RestController
@RequestMapping("/api/blockchain")
class BlockchainController(
    private val getAddressTransactionsUseCase: GetAddressTransactionsUseCase,
    private val blockchainService: BlockchainService
) {

    @GetMapping("/{network}/address/{address}")
    suspend fun getAddress(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @PathVariable network: Network,
        @PathVariable address: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<AddressResponse> {
        val userId = userDetails?.let { UUID.fromString(it.username) }
        val response = blockchainService.getAddress(userId, address, network, page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping(
        value = ["/api/blockchain/{network}/address/{address}/transactions"],
        produces = [org.springframework.http.MediaType.APPLICATION_NDJSON_VALUE]
    )
    fun getAddressTransactions(
        @PathVariable network: Network,
        @PathVariable address: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): Flow<TransactionResponse> {
        val userId = userDetails?.username?.let { UUID.fromString(it) }
        return getAddressTransactionsUseCase.execute(userId, address, network, page, size)
    }

    @GetMapping("/{network}/tx/{hash}")
    suspend fun getTransaction(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @PathVariable network: Network,
        @PathVariable hash: String
    ): ResponseEntity<TransactionResponse> {
        val userId = userDetails?.let { UUID.fromString(it.username) }
        val response = blockchainService.getTransactionByHash(hash, network, userId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{network}/block/{numberOrHash}")
    suspend fun getBlock(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @PathVariable network: Network,
        @PathVariable numberOrHash: String
    ): ResponseEntity<BlockResponse> {
        val userId = userDetails?.let { UUID.fromString(it.username) }
        val response = blockchainService.getBlock(numberOrHash, network, userId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{network}/block/latest")
    suspend fun getLatestBlock(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @PathVariable network: Network
    ): ResponseEntity<BlockResponse> {
        val userId = userDetails?.let { UUID.fromString(it.username) }
        val response = blockchainService.getLatestBlock(network, userId)
        return ResponseEntity.ok(response)
    }
}