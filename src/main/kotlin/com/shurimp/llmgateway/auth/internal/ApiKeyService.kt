package com.shurimp.llmgateway.auth.internal

import com.shurimp.llmgateway.auth.api.ApiKeyPrincipal
import com.shurimp.llmgateway.auth.domain.ApiKey
import com.shurimp.llmgateway.auth.infrastructure.ApiKeyRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest

data class IssuedApiKey(
    val id: Long,
    val label: String,
    val rawKey: String,
)

@Service
class ApiKeyService(
    private val apiKeyRepository: ApiKeyRepository,
) {

    @Transactional
    fun issue(label: String): IssuedApiKey {
        repeat(3) {
            val generated = ApiKeyGenerator.generate()
            try {
                val saved = apiKeyRepository.saveAndFlush(
                    ApiKey(label = label, keyPrefix = generated.keyPrefix, keyHash = generated.keyHash)
                )
                return IssuedApiKey(id = saved.id!!, label = saved.label, rawKey = generated.raw)
            } catch(e: DataIntegrityViolationException) {
                // key_prefix unique 충돌 - 재시도로 방어
            }
        }
        throw AuthException(AuthErrorCode.KEY_GENERATION_FAILED)
    }

    @Transactional
    fun revoke(id: Long) {
        val apiKey = apiKeyRepository.findById(id)
            .orElseThrow { AuthException(AuthErrorCode.API_KEY_NOT_FOUND) }
        apiKey.revoke()
    }

    @Transactional(readOnly = true)
    fun list(): List<ApiKey> = apiKeyRepository.findAll()

    @Transactional(readOnly = true)
    fun verify(rawKey: String): ApiKeyPrincipal {
        val hash = ApiKeyGenerator.sha256Hex(rawKey)

        val apiKey = apiKeyRepository.findByKeyPrefix(rawKey.take(14))
            ?.takeIf { it.active }
            ?.takeIf { MessageDigest.isEqual(it.keyHash.toByteArray(), hash.toByteArray()) }
            ?: throw BadCredentialsException("invalid api key")

        return ApiKeyPrincipal(apiKey.id!!, apiKey.label)
    }
}