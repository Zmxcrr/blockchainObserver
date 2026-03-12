package com.blockchain.infrastructure.db.entity

import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("users")
data class UserEntity(
    @Id private val id: UUID,
    val email: String,
    @Column("password_hash") val passwordHash: String,
    val username: String,
    @Column("created_at") val createdAt: LocalDateTime
) : Persistable<UUID> {

    @Transient
    @org.springframework.data.annotation.Transient
    var isNewRecord: Boolean = false

    override fun getId(): UUID = id

    override fun isNew(): Boolean = isNewRecord
}