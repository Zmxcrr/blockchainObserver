package com.blockchain.domain.entity

import com.blockchain.domain.enum.Network
import com.blockchain.domain.enum.SearchType
import java.util.UUID

data class SearchHistory(
    val id: UUID,
    val userId: UUID,
    val query: String,
    val network: Network,
    val searchType: SearchType,
    val createdAt: java.time.Instant = java.time.Instant.now()
)