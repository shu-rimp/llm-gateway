package com.shurimp.llmgateway.auth.infrastructure

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "llm-gateway.auth")
data class AuthProperties(
    val admin: Admin
) {
    data class Admin(
        val username: String,
        val passwordHash: String,
    )
}
