package com.shurimp.llmgateway

import com.shurimp.llmgateway.auth.api.ApiKeyPrincipal
import com.shurimp.llmgateway.auth.infrastructure.ApiKeyAuthenticationProvider
import com.shurimp.llmgateway.auth.infrastructure.ApiKeyAuthenticationToken
import com.shurimp.llmgateway.auth.internal.ApiKeyService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

@ExtendWith(MockitoExtension::class)
class ApiKeyAuthenticationProviderTest {
    @Mock lateinit var apiKeyService: ApiKeyService
    @InjectMocks lateinit var provider: ApiKeyAuthenticationProvider

    @Test
    fun `verify 성공 시 인증 토큰 반환`() {
        val principal = ApiKeyPrincipal(1L, "test")
        val unauthenticatedToken = ApiKeyAuthenticationToken.unauthenticated("llmgw_abcd1234")
        given(apiKeyService.verify("llmgw_abcd1234")).willReturn(principal)

        val result = provider.authenticate(unauthenticatedToken)

        assertThat(result.isAuthenticated).isTrue()
        assertThat(result.principal).isEqualTo(principal)
    }

    @Test
    fun `verify 실패 시 예외 전파`() {
        val unauthenticatedToken = ApiKeyAuthenticationToken.unauthenticated("wrong")
        given(apiKeyService.verify("wrong")).willThrow(BadCredentialsException("invalid api key"))

        assertThatThrownBy { provider.authenticate(unauthenticatedToken) }
            .isInstanceOf(BadCredentialsException::class.java)
    }

    @Test
    fun `supports는 ApiKeyAuthenticationToken만 true`() {
        assertThat(provider.supports(ApiKeyAuthenticationToken::class.java)).isTrue()
        assertThat(provider.supports(UsernamePasswordAuthenticationToken::class.java)).isFalse()
    }
}