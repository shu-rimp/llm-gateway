package com.shurimp.llmgateway.shared

data class ErrorDetail(
    val message: String,
    val type: String,
    val code: String,
)
