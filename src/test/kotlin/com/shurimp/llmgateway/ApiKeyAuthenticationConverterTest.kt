package com.shurimp.llmgateway

import com.shurimp.llmgateway.auth.infrastructure.ApiKeyAuthenticationConverter
import com.shurimp.llmgateway.auth.infrastructure.ApiKeyAuthenticationToken
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest

class ApiKeyAuthenticationConverterTest {
    private val converter = ApiKeyAuthenticationConverter()

    @Test
    fun `Authorization 헤더가 없으면 null`() {
        val request = MockHttpServletRequest()

        assertThat(converter.convert(request)).isNull()
    }

    @Test
    fun `Bearer가 아닌 scheme이면 null`() {
        val request = MockHttpServletRequest()
        request.addHeader(HttpHeaders.AUTHORIZATION, "not Bearer")

        assertThat(converter.convert(request)).isNull()
    }

    @Test
    fun `Bearer scheme은 대소문자 무관하게 인식`() {
        val request = MockHttpServletRequest()
        request.addHeader(HttpHeaders.AUTHORIZATION, "BEARER abcd1234")

        assertThat(converter.convert(request))
            .isInstanceOf(ApiKeyAuthenticationToken::class.java)
    }

    @Test
    fun `빈 값이면 null`() {
        val request = MockHttpServletRequest()
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer ")

        assertThat(converter.convert(request)).isNull()
    }

    @Test
    fun `정상 헤더는 미인증 토큰을 생성`() {
        val request = MockHttpServletRequest()
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer abcd1234")

        assertThat(converter.convert(request))
            .isInstanceOf(ApiKeyAuthenticationToken::class.java)
    }
}