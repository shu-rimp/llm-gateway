package com.shurimp.llmgateway.auth.internal

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.HexFormat

data class GeneratedApiKey(
    val raw: String,
    val keyPrefix: String,
    val keyHash: String,
)

object ApiKeyGenerator {
    private const val PREFIX = "llmgw_"
    private const val PREFIX_LOOKUP_LENGTH = 14
    private val secureRandom = SecureRandom()

    fun generate(): GeneratedApiKey {
        val bytes = ByteArray(32).also { secureRandom.nextBytes(it) }
        val raw = PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        return GeneratedApiKey(
            raw = raw,
            keyPrefix = raw.take(PREFIX_LOOKUP_LENGTH),
            keyHash = sha256Hex(raw)
        )
    }

    fun sha256Hex(raw: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
        return HexFormat.of().formatHex(digest)
    }
}
