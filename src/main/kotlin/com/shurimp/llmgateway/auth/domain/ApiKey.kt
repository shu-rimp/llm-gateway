package com.shurimp.llmgateway.auth.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Generated
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.generator.EventType
import org.hibernate.type.SqlTypes
import java.time.Instant

@Entity
@Table(name = "api_keys")
class ApiKey(
    @Column(nullable = false)
    var label: String,

    @Column(name = "key_prefix", nullable = false, unique = true)
    val keyPrefix: String,

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "key_hash", nullable = false, length = 64)
    val keyHash: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    @Column(nullable = false)
    var active: Boolean = true
        protected set

    @Generated(event = [EventType.INSERT])
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    var createdAt: Instant? = null
        protected set

    fun revoke() { active = false }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ApiKey) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}