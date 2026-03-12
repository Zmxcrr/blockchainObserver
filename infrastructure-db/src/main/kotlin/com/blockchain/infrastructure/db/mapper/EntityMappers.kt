package com.blockchain.infrastructure.db.mapper

import com.blockchain.domain.entity.FavoriteAddress
import com.blockchain.domain.entity.SearchHistory
import com.blockchain.domain.entity.Transaction
import com.blockchain.domain.entity.TransactionStatus
import com.blockchain.domain.entity.User
import com.blockchain.domain.enum.Network
import com.blockchain.domain.enum.SearchType
import com.blockchain.infrastructure.db.entity.FavoriteAddressEntity
import com.blockchain.infrastructure.db.entity.SearchHistoryEntity
import com.blockchain.infrastructure.db.entity.TransactionEntity
import com.blockchain.infrastructure.db.entity.UserEntity
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

private fun Instant.toLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(this, ZoneOffset.UTC)
private fun LocalDateTime.toInstant(): Instant = this.toInstant(ZoneOffset.UTC)

fun UserEntity.toDomain() = User(
    id = id,
    email = email,
    passwordHash = passwordHash,
    username = username,
    createdAt = createdAt.toInstant()
)

fun User.toEntity(isNew: Boolean = false) = UserEntity(
    id = id,
    email = email,
    passwordHash = passwordHash,
    username = username,
    createdAt = createdAt.toLocalDateTime()
).apply {
    this.isNewRecord = isNew
}

fun FavoriteAddressEntity.toDomain() = FavoriteAddress(
    id = id,
    userId = userId,
    address = address,
    network = Network.valueOf(network),
    label = label,
    createdAt = createdAt.toInstant()
)

fun FavoriteAddress.toEntity(isNew: Boolean = false) = FavoriteAddressEntity(
    id = id,
    userId = userId,
    address = address,
    network = network.name,
    label = label,
    createdAt = createdAt.toLocalDateTime()
).apply {
    this.isNewRecord = isNew
}

fun SearchHistoryEntity.toDomain() = SearchHistory(
    id = id,
    userId = userId,
    query = query,
    network = Network.valueOf(network),
    searchType = SearchType.valueOf(searchType),
    createdAt = createdAt.toInstant()
)

fun SearchHistory.toEntity(isNew: Boolean = false) = SearchHistoryEntity(
    id = id,
    userId = userId,
    query = query,
    network = network.name,
    searchType = searchType.name,
    createdAt = createdAt.toLocalDateTime()
).apply {
    this.isNewRecord = isNew
}

fun TransactionEntity.toDomain() = Transaction(
    hash = hash,
    network = Network.valueOf(network),
    fromAddress = fromAddress,
    toAddress = toAddress,
    amount = amount,
    fee = fee,
    blockNumber = blockNumber,
    blockHash = blockHash,
    timestamp = timestamp.toInstant(),
    status = TransactionStatus.valueOf(status),
    contractAddress = contractAddress
)

fun Transaction.toEntity() = TransactionEntity(
    hash = hash,
    network = network.name,
    fromAddress = fromAddress,
    toAddress = toAddress,
    amount = amount,
    fee = fee,
    blockNumber = blockNumber,
    blockHash = blockHash,
    timestamp = timestamp.toLocalDateTime(),
    status = status.name,
    contractAddress = contractAddress
)