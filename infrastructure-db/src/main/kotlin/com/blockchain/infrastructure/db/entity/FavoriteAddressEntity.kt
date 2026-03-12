package com.blockchain.infrastructure.db.entity

import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("favorite_addresses")
data class FavoriteAddressEntity(
    @Id private val id: UUID,
    @Column("user_id") val userId: UUID,
    val address: String,
    val network: String,
    val label: String?,
    @Column("created_at") val createdAt: LocalDateTime
) : Persistable<UUID> {

    @Transient
    @org.springframework.data.annotation.Transient
    var isNewRecord: Boolean = false

    override fun getId(): UUID = id
    override fun isNew(): Boolean = isNewRecord
}