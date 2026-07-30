package com.shurimp.llmgateway.routing.internal

import org.springframework.http.HttpStatus

enum class RoutingErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String,
) {
    STREAMING_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "STREAMING_NOT_SUPPORTED", "streaming is not supported in v1"),
    ;
}