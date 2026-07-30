package com.shurimp.llmgateway.routing.domain

class ProviderCallException(
    message: String,
    cause: Throwable? = null,
): RuntimeException(message, cause)