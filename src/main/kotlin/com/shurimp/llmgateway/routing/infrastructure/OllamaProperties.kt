package com.shurimp.llmgateway.routing.infrastructure

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "llm-gateway.ollama")
data class OllamaProperties(
    val baseUrl: String,
    val model: String,
    val readTimeout: Duration,
)
