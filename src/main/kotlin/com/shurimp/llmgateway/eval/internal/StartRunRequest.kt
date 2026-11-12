package com.shurimp.llmgateway.eval.internal

import jakarta.validation.constraints.NotBlank

data class StartRunRequest(
    @field:NotBlank
    val targetModel: String,
)