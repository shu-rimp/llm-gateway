package com.shurimp.llmgateway.routing.internal

import com.shurimp.llmgateway.routing.domain.ProviderCallException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException

import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ChatCompletionExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(ChatCompletionExceptionHandler::class.java)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = e.bindingResult.fieldErrors
            .firstOrNull()?.defaultMessage ?: "invalid request"

        // 의도된 거절: debug레벨로 남긴다.
        log.debug("validation failed: {}", message)

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

        // 의도된 거절: debug레벨로 남긴다.
        log.debug("routing rejected request: {}", errorCode.code)

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

    @ExceptionHandler(ProviderCallException::class)
    fun handleProviderCallException(e: ProviderCallException): ResponseEntity<ErrorResponse> {
        log.error("provider call failed", e)

        val body = ErrorResponse(
            error = ErrorDetail(
                message = "failed to call the model provider",
                type = "PROVIDER_ERROR",
                code = "PROVIDER_ERROR",
            )
        )

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body)
    }
}