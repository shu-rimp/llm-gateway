package com.shurimp.llmgateway.eval.internal

import org.springframework.http.HttpStatus

enum class EvalErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String,
) {
    UNKNOWN_MODEL(HttpStatus.BAD_REQUEST, "UNKNOWN_MODEL", "requested model is not supported"),
    ;
}