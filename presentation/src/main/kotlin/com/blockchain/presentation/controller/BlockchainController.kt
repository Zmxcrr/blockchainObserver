package com.blockchain.presentation.controller

import com.blockchain.application.dto.AddressResponse
import com.blockchain.application.dto.BlockResponse
import com.blockchain.application.dto.PagedResponse
import com.blockchain.application.dto.TransactionResponse
import com.blockchain.application.service.BlockchainService
import com.blockchain.application.usecase.GetAddressTransactionsUseCase
import com.blockchain.domain.enum.Network
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/api/blockchain")
class BlockchainController(
    private val getAddressTransactionsUseCase: GetAddressTransactionsUseCase,
    private val blockchainService: BlockchainService
) {

    @GetMapping("/{network}/address/{address}")
    fun getAddress(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @PathVariable network: Network,
        @PathVariable address: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): Mono<ResponseEntity<AddressResponse>> {
        val userId = userDetails?.let { UUID.fromString(it.username) }
        return blockchainService.getAddress(userId, address, network, page, size)
            .map { ResponseEntity.ok(it) }
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
    ): Flux<TransactionResponse> {
        val userId = userDetails?.username?.let { UUID.fromString(it) }
        return getAddressTransactionsUseCase.execute(userId, address, network, page, size)
    }

    @GetMapping("/{network}/tx/{hash}")
    fun getTransaction(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @PathVariable network: Network,
        @PathVariable hash: String
    ): Mono<ResponseEntity<TransactionResponse>> {
        val userId = userDetails?.let { UUID.fromString(it.username) }
        return blockchainService.getTransactionByHash(hash, network, userId)
            .map { ResponseEntity.ok(it) }
    }

    @GetMapping("/{network}/block/{numberOrHash}")
    fun getBlock(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @PathVariable network: Network,
        @PathVariable numberOrHash: String
    ): Mono<ResponseEntity<BlockResponse>> {
        val userId = userDetails?.let { UUID.fromString(it.username) }
        return blockchainService.getBlock(numberOrHash, network, userId)
            .map { ResponseEntity.ok(it) }
    }

    @GetMapping("/{network}/block/latest")
    fun getLatestBlock(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @PathVariable network: Network
    ): Mono<ResponseEntity<BlockResponse>> {
        val userId = userDetails?.let { UUID.fromString(it.username) }
        return blockchainService.getLatestBlock(network, userId)
            .map { ResponseEntity.ok(it) }
    }
}