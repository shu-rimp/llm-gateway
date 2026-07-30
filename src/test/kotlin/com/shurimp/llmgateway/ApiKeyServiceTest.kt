package com.shurimp.llmgateway

import com.shurimp.llmgateway.auth.domain.ApiKey
import com.shurimp.llmgateway.auth.infrastructure.ApiKeyRepository
import com.shurimp.llmgateway.auth.internal.ApiKeyGenerator
import com.shurimp.llmgateway.auth.internal.ApiKeyService
import com.shurimp.llmgateway.auth.internal.AuthErrorCode
import com.shurimp.llmgateway.auth.internal.AuthException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.test.util.ReflectionTestUtils
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ApiKeyServiceTest {

    @Mock lateinit var apiKeyRepository: ApiKeyRepository
    @InjectMocks lateinit var apiKeyService: ApiKeyService

    @Test
    fun `issue는 IssuedApiKey를 반환함`() {
        given(apiKeyRepository.saveAndFlush(any()))
            .willAnswer { invocation ->
                val apiKey = invocation.getArgument<ApiKey>(0)
                ReflectionTestUtils.setField(apiKey, "id", 1L)
                apiKey
            }

        val issued = apiKeyService.issue("test-key")

        assertThat(issued.id).isEqualTo(1L)
        assertThat(issued.label).isEqualTo("test-key")
        assertThat(issued.rawKey).startsWith("llmgw_")
    }

    @Test
    fun `revoke는 대상 키를 비활성화함`() {
        val apiKey = ApiKey("1", "1", "1")
        given(apiKeyRepository.findById(1L))
            .willReturn(Optional.of(apiKey))

        apiKeyService.revoke(1L)

        assertThat(apiKey.active).isFalse()
    }

    @Test
    fun `존재하지 않는 키를 revoke 하면 예외를 던진다`() {
        given(apiKeyRepository.findById(1L))
            .willReturn(Optional.empty())

        assertThatThrownBy { apiKeyService.revoke(1L) }
            .isInstanceOf(AuthException::class.java)
            .extracting("errorCode").isEqualTo(AuthErrorCode.API_KEY_NOT_FOUND)
    }

    @Test
    fun `존재하지 않는 prefix에 대해 verify 예외`() {
        given(apiKeyRepository.findByKeyPrefix("ghost"))
            .willReturn(null)

        assertThatThrownBy { apiKeyService.verify("ghost") }
            .isInstanceOf(BadCredentialsException::class.java)
            .hasMessage("invalid api key")
    }

    @Test
    fun `비활성 키에 대해 verify 예외`() {
        val generatedKey = ApiKeyGenerator.generate()
        val apiKey = ApiKey(label = "test", keyPrefix = generatedKey.keyPrefix, keyHash = generatedKey.keyHash)
        apiKey.revoke()
        given(apiKeyRepository.findByKeyPrefix(generatedKey.keyPrefix))
            .willReturn(apiKey)

        assertThatThrownBy { apiKeyService.verify(generatedKey.raw) }
            .isInstanceOf(BadCredentialsException::class.java)
            .hasMessage("invalid api key")
    }

    @Test
    fun `해시 불일치 키에 대해 verify 예외`() {
        val generatedKey = ApiKeyGenerator.generate()
        val apiKey = ApiKey(label = "test", keyPrefix = generatedKey.keyPrefix, keyHash = generatedKey.keyHash)
        given(apiKeyRepository.findByKeyPrefix(generatedKey.keyPrefix))
            .willReturn(apiKey)

        // 맨 뒷 글자만 바꿔 조회는 되지만 해시가 다른 경우
        val manipulatedKeyRaw = generatedKey.raw.dropLast(1) + if (generatedKey.raw.last() == 'A') 'B' else 'A'

        assertThatThrownBy { apiKeyService.verify(manipulatedKeyRaw) }
            .isInstanceOf(BadCredentialsException::class.java)
            .hasMessage("invalid api key")
    }

    @Test
    fun `올바른 raw 키는 ApiKeyPrincipal을 반환함`() {
        val generatedKey = ApiKeyGenerator.generate()
        val apiKey = ApiKey(label = "test", keyPrefix = generatedKey.keyPrefix, keyHash = generatedKey.keyHash)
        ReflectionTestUtils.setField(apiKey, "id", 12L)
        given(apiKeyRepository.findByKeyPrefix(generatedKey.keyPrefix))
            .willReturn(apiKey)

        val principal = apiKeyService.verify(generatedKey.raw)

        assertThat(principal.id).isEqualTo(12L)
        assertThat(principal.label).isEqualTo(apiKey.label)
    }
}