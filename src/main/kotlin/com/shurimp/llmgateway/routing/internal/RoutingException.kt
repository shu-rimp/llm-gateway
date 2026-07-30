package com.shurimp.llmgateway.routing.internal

class RoutingException(val errorCode: RoutingErrorCode) : RuntimeException(errorCode.message)