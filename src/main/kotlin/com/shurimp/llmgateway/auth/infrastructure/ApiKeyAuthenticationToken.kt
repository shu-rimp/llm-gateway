package com.shurimp.llmgateway.auth.infrastructure

import com.shurimp.llmgateway.auth.api.ApiKeyPrincipal
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

class ApiKeyAuthenticationToken private constructor(
    private val rawKey: String?,
    private val principal: ApiKeyPrincipal?,
    authorities: Collection<GrantedAuthority>,
) : AbstractAuthenticationToken(authorities) {
    override fun getCredentials(): Any? = rawKey
    override fun getPrincipal(): Any? = principal

    companion object {
        fun unauthenticated(rawKey: String) =
            ApiKeyAuthenticationToken(rawKey, null, emptyList())

        fun authenticated(principal: ApiKeyPrincipal) =
            ApiKeyAuthenticationToken(null, principal, listOf(SimpleGrantedAuthority("ROLE_API_KEY")))
                .apply { isAuthenticated = true }
    }
}