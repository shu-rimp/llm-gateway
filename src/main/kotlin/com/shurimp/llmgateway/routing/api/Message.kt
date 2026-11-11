package com.shurimp.llmgateway.routing.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true) // reasoning 드롭
data class Message(
    @Schema(example = "user")
    @field:Pattern(regexp = "system|user|assistant")
    val role: String,
    val content: String,
)
