package com.shurimp.llmgateway.routing.internal

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException

import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ChatCompletionExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = e.bindingResult.fieldErrors
            .firstOrNull()?.defaultMessage ?: "invalid request"

        val body = ErrorResponse(
            error = ErrorDetail(
                message = message,
                type = "INVALID_REQUEST_ERROR",
                code = "INVALID_REQUEST"
            )
        )

        return ResponseEntity.badRequest().body(body)
    }

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