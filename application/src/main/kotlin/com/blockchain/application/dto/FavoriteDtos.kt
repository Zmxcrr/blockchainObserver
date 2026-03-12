package com.blockchain.application.dto

import com.blockchain.domain.enum.Network

data class AddFavoriteRequest(
    val address: String,
    val network: Network,
    val label: String? = null
)

data class FavoriteResponse(
    val id: String,
    val address: String,
    val network: Network,
    val label: String?
)