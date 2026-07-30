package com.shurimp.llmgateway

import com.shurimp.llmgateway.auth.internal.ApiKeyGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ApiKeyGeneratorTest {

    @Test
    fun `keyPrefix는 raw의 앞부분과 일치한다`() {
        val generatedKey = ApiKeyGenerator.generate()

        assertThat(generatedKey.keyPrefix).hasSize(14)
        assertThat(generatedKey.keyPrefix).startsWith("llmgw_")
        assertThat(generatedKey.raw).startsWith(generatedKey.keyPrefix)
    }

    @Test
    fun `같은 raw는 같은 해시, 다른 raw는 다른 해시를 만든다`() {
        val raw1 = "llmgw_abcd1234"
        val raw2 = "llmgw_efgh5678"

        val hash1FromRaw1 = ApiKeyGenerator.sha256Hex(raw1)
        val hash2FromRaw1 = ApiKeyGenerator.sha256Hex(raw1)
        val hash1FromRaw2 = ApiKeyGenerator.sha256Hex(raw2)

        assertThat(hash1FromRaw1).isEqualTo(hash2FromRaw1)
        assertThat(hash1FromRaw1).isNotEqualTo(hash1FromRaw2)
    }

    @Test
    fun `100번 생성해도 prefix가 중복되지 않는다`() {
        val prefixes = (1..100).map { ApiKeyGenerator.generate().keyPrefix }

        assertThat(prefixes.toSet()).hasSize(100)
    }
}