package com.shurimp.llmgateway.eval.internal

import com.shurimp.llmgateway.shared.ErrorDetail
import com.shurimp.llmgateway.shared.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class EvalExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(EvalExceptionHandler::class.java)
    }

    @ExceptionHandler(EvalException::class)
    fun handle(e: EvalException): ResponseEntity<ErrorResponse> {
        val errorCode = e.errorCode
        log.debug("eval rejected request: {}", errorCode.code)

        val body = ErrorResponse(
            error = ErrorDetail(
                message = errorCode.message,
                type = "INVALID_REQUEST_ERROR",
                code = errorCode.code,
            )
        )
        return ResponseEntity.status(errorCode.status).body(body)
    }
}