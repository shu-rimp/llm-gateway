package com.shurimp.llmgateway.routing.api

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonNaming

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ChatCompletionRequest(
    val model: String,
    @field:Valid
    @field:NotEmpty
    val messages: List<Message>,
    val temperature: Double? = null,
    @JsonProperty("max_tokens")
    @field:Positive
    val maxTokens: Int? = null, // max_tokens
    val stream: Boolean = false,
)