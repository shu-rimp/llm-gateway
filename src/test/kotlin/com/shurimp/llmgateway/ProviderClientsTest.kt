package com.shurimp.llmgateway

import com.shurimp.llmgateway.routing.infrastructure.OpenAiCompatibleClient
import com.shurimp.llmgateway.routing.infrastructure.Provider
import com.shurimp.llmgateway.routing.infrastructure.ProviderClients
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestClient
import java.time.Duration

class ProviderClientsTest {

    private val clients = ProviderClients(listOf(
        client("ollama", "gemma4:12b", "gemma4:26b"),
        client("gemini", "gemini-3.5-flash-lite")
    ))

    @Test
    fun `ollama 모델은 ollama 클라이언트를 고른다`() {
        assertThat(clients.forModel("gemma4:26b")?.name).isEqualTo("ollama")
    }

    @Test
    fun `gemini 모델은 gemini 클라이언트를 고른다`() {
        assertThat(clients.forModel("gemini-3.5-flash-lite")?.name).isEqualTo("gemini")
    }

    @Test
    fun `지원하지 않는 모델은 null을 돌려준다`() {
        assertThat(clients.forModel("gpt-4o")).isNull()
    }

    @Test
    fun `모델명의 일부만 일치하면 null을 돌려준다`() {
        assertThat(clients.forModel("gemma4")).isNull()
    }

    private fun client(name: String, vararg models: String) =
        OpenAiCompatibleClient(
            name = name,
            restClient = RestClient.create(),
            provider = Provider(
                baseUrl = "http://localhost",
                chatPath = "/v1/chat/completions",
                models = models.toList(),
                readTimeout = Duration.ofSeconds(2),
            )
        )
}