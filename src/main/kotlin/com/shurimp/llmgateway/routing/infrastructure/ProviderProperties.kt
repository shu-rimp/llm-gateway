package com.shurimp.llmgateway.routing.infrastructure

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "llm-gateway")
data class ProviderProperties(
    val providers: Map<String, Provider>,
)
