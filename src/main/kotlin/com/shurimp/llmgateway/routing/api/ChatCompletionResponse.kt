package com.shurimp.llmgateway.routing.api

import com.fasterxml.jackson.annotation.JsonProperty
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ChatCompletionResponse(
    @get:JsonProperty("object") val objectType: String, // object는 코틀린 예약어
    val id: String,
    val created: Long,
    val model: String,
    val systemFingerprint: String? = null,
    val choices: List<Choice>,
    val usage: Usage,
)
