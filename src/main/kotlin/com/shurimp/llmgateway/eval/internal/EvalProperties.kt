package com.shurimp.llmgateway.eval.internal

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "llm-gateway.eval")
data class EvalProperties(
    val judgeModel: String,
)