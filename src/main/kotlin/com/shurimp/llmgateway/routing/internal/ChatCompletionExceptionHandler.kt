package com.shurimp.llmgateway.routing.internal

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ChatCompletionExceptionHandler {

    @ExceptionHandler(RoutingException::class)
    fun handle(e: RoutingException): ResponseEntity<ErrorResponse> {
        val errorCode = e.errorCode

        val type = when (errorCode.status) {
            HttpStatus.BAD_REQUEST -> "INVALID_REQUEST_ERROR"
            HttpStatus.BAD_GATEWAY -> "PROVIDER_ERROR"
            else -> "INTERNAL_ERROR"
        }

        val body = ErrorResponse(
            error = ErrorDetail(
                message = errorCode.message,
                type = type,
                code = errorCode.code,
            )
        )

        return ResponseEntity.status(errorCode.status).body(body)
    }
}