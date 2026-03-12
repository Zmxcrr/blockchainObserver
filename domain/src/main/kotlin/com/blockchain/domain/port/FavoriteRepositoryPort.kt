package com.blockchain.domain.port

import com.blockchain.domain.entity.FavoriteAddress
import com.blockchain.domain.enum.Network
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface FavoriteRepositoryPort {
    fun save(favorite: FavoriteAddress): Mono<FavoriteAddress>
    fun findByUserId(userId: UUID): Flux<FavoriteAddress>

    fun findByIdAndUserId(id: UUID, userId: UUID): Mono<FavoriteAddress>

    fun findByUserIdAndAddressAndNetwork(userId: UUID, address: String, network: Network): Mono<FavoriteAddress>
    fun deleteById(id: UUID): Mono<Void>
    fun existsByUserIdAndAddressAndNetwork(userId: UUID, address: String, network: Network): Mono<Boolean>
}