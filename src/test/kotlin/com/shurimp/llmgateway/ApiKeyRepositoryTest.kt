package com.shurimp.llmgateway

import com.shurimp.llmgateway.auth.domain.ApiKey
import com.shurimp.llmgateway.auth.infrastructure.ApiKeyRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration::class)
class ApiKeyRepositoryTest(@Autowired private val apiKeyRepository: ApiKeyRepository) {

    @Test
    fun `저장 후 findByKeyPrefix로 조회`() {
        val saved = apiKeyRepository.save(ApiKey(label = "test", keyPrefix = "llmgw_abcd1234", keyHash = "x".repeat(64)))

        val found = apiKeyRepository.findByKeyPrefix("llmgw_abcd1234")

        assertThat(found?.id).isEqualTo(saved.id)
        assertThat(found?.createdAt).isNotNull()
        assertThat(found?.active).isTrue()
    }
}