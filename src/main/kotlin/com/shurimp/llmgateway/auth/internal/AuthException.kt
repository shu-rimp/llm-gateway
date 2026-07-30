package com.shurimp.llmgateway.auth.internal

class AuthException(val errorCode: AuthErrorCode) : RuntimeException(errorCode.message)