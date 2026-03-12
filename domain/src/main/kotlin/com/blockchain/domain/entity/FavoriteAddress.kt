package com.blockchain.domain.entity

import com.blockchain.domain.enum.Network
import java.util.UUID

data class FavoriteAddress(
    val id: UUID,
    val userId: UUID,
    val address: String,
    val network: Network,
    val label: String? = null,
    val createdAt: java.time.Instant = java.time.Instant.now()
)