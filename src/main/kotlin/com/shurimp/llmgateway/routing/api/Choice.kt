package com.shurimp.llmgateway.routing.api

import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Choice(
    val index: Int,
    val message: Message,
    val finishReason: String?,
)
