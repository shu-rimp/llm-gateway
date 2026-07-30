package com.shurimp.llmgateway.auth.infrastructure

import com.shurimp.llmgateway.auth.domain.ApiKey
import org.springframework.data.jpa.repository.JpaRepository

interface ApiKeyRepository : JpaRepository<ApiKey, Long> {
    fun findByKeyPrefix(keyPrefix: String): ApiKey?
}