package com.shurimp.llmgateway.eval.internal

import com.shurimp.llmgateway.shared.ErrorDetail
import com.shurimp.llmgateway.shared.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(basePackageClasses = [EvalRunController::class])
class EvalExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(EvalExceptionHandler::class.java)
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

    @ExceptionHandler(EvalException::class)
    fun handle(e: EvalException): ResponseEntity<ErrorResponse> {
        val errorCode = e.errorCode
        log.debug("eval rejected request: {}", errorCode.code)

        val type = when (errorCode.status) {
            HttpStatus.BAD_REQUEST -> "INVALID_REQUEST_ERROR"
            HttpStatus.NOT_FOUND -> "NOT_FOUND_ERROR"
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