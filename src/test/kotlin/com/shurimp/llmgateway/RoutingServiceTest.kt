package com.shurimp.llmgateway

import com.shurimp.llmgateway.routing.api.ChatCompletionRequest
import com.shurimp.llmgateway.routing.api.Message
import com.shurimp.llmgateway.routing.infrastructure.OpenAiCompatibleClient
import com.shurimp.llmgateway.routing.infrastructure.Provider
import com.shurimp.llmgateway.routing.infrastructure.ProviderClients
import com.shurimp.llmgateway.routing.internal.RoutingErrorCode
import com.shurimp.llmgateway.routing.internal.RoutingException
import com.shurimp.llmgateway.routing.internal.RoutingService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.assertj.core.api.Assertions.assertThat
import org.springframework.web.client.RestClient
import java.time.Duration

class RoutingServiceTest {

    private val clients = ProviderClients(listOf(
        client("ollama", "gemma4:12b", "gemma4:26b"),
        client("gemini", "gemini-3.5-flash-lite")
    ))
    private val service = RoutingService(clients)

    @Test
    fun `지원하지 않는 모델이면 UNKNOWN_MODEL 예외를 던진다`() {
        val e = assertThrows<RoutingException> {
            service.routeChatCompletion(sampleRequest("gpt-4o"))
        }

        assertThat(e.errorCode).isEqualTo(RoutingErrorCode.UNKNOWN_MODEL)
    }

    private fun sampleRequest(model: String = "gemma4:12b") = ChatCompletionRequest (
        model = model,
        messages = listOf(
            Message(
                role = "user",
                content = "hi"
            )
        ),
    )

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