package com.shurimp.llmgateway.routing.infrastructure

import java.time.Duration

data class Provider(
    val baseUrl: String,
    val chatPath: String,
    val apiKey: String? = null,
    val models: List<String>,
    val readTimeout: Duration,
)
