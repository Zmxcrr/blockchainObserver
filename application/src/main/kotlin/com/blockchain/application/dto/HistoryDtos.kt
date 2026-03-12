package com.blockchain.application.dto

import com.blockchain.domain.enum.Network
import com.blockchain.domain.enum.SearchType

data class SearchHistoryResponse(
    val id: String,
    val query: String,
    val network: Network,
    val searchType: SearchType,
    val createdAt: String
)