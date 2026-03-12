package com.blockchain.application.usecase

import com.blockchain.application.dto.PagedResponse
import com.blockchain.application.dto.TransactionResponse
import com.blockchain.application.service.BlockchainService
import com.blockchain.domain.enum.Network
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class GetAddressTransactionsUseCase(private val blockchainService: BlockchainService) {
    fun execute(
        userId: UUID?,
        address: String,
        network: Network,
        page: Int = 0,
        size: Int = 20
    ): Flux<TransactionResponse> =
        blockchainService.getAddressTransactions(userId, address, network, page, size)
}