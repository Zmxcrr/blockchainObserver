package com.blockchain.infrastructure.db.repository

import com.blockchain.infrastructure.db.entity.FavoriteAddressEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
interface FavoriteR2dbcRepository : ReactiveCrudRepository<FavoriteAddressEntity, UUID> {
    fun findByUserId(userId: UUID): Flux<FavoriteAddressEntity>

    fun findByIdAndUserId(id: UUID, userId: UUID): Mono<FavoriteAddressEntity>

    @Query("SELECT * FROM favorite_addresses WHERE user_id = :userId AND address = :address AND network = :network LIMIT 1")
    fun findByUserIdAndAddressAndNetwork(userId: UUID, address: String, network: String): Mono<FavoriteAddressEntity>

    @Query("SELECT COUNT(*) > 0 FROM favorite_addresses WHERE user_id = :userId AND address = :address AND network = :network")
    fun existsByUserIdAndAddressAndNetwork(userId: UUID, address: String, network: String): Mono<Boolean>
}