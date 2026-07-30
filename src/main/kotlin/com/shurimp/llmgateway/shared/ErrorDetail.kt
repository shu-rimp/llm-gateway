package com.shurimp.llmgateway.routing.internal

data class ErrorDetail(
    val message: String,
    val type: String,
    val code: String,
)
