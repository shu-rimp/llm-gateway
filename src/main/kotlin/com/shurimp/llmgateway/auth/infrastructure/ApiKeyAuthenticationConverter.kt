package com.shurimp.llmgateway.auth.infrastructure

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationConverter

class ApiKeyAuthenticationConverter : AuthenticationConverter {

    override fun convert(request: HttpServletRequest): Authentication? {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION) ?: return null
        val parts = header.split(" ", limit = 2)
        if (parts.size != 2) return null

        val (scheme, rawKey) = parts
        if (!scheme.equals("Bearer", ignoreCase = true) || rawKey.isBlank()) return null

        return ApiKeyAuthenticationToken.unauthenticated(rawKey.trim())
    }
}