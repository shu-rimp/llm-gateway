package com.shurimp.llmgateway.auth.internal

import org.springframework.http.HttpStatus

enum class AuthErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String,
) {
    API_KEY_NOT_FOUND(HttpStatus.NOT_FOUND, "API_KEY_NOT_FOUND", "api key not found"),
    KEY_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "KEY_GENERATION_FAILED", "failed to generate a unique api key"),
    ;
}
