package com.shurimp.llmgateway.auth.infrastructure

import com.shurimp.llmgateway.auth.internal.ApiKeyService
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication

class ApiKeyAuthenticationProvider(
    private val apiKeyService: ApiKeyService,
) : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication {
        val rawKey = authentication.credentials as String
        val principal = apiKeyService.verify(rawKey)
        return ApiKeyAuthenticationToken.authenticated(principal)
    }

    override fun supports(authentication: Class<*>): Boolean =
        ApiKeyAuthenticationToken::class.java.isAssignableFrom(authentication)
}