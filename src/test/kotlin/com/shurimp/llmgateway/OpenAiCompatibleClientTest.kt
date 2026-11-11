package com.shurimp.llmgateway

import com.shurimp.llmgateway.routing.api.ChatCompletionRequest
import com.shurimp.llmgateway.routing.api.Message
import com.shurimp.llmgateway.routing.domain.ProviderCallException
import com.shurimp.llmgateway.routing.infrastructure.OpenAiCompatibleClient
import com.shurimp.llmgateway.routing.infrastructure.Provider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withServerError
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import java.time.Duration

class OpenAiCompatibleClientTest {

    @Test
    fun `실제 Ollama 응답 형태를 정상 파싱한다`() {
        val(client, server) = clientWithServer()
        val fixture = javaClass.getResourceAsStream("/ollama-sample-response.json")!!.bufferedReader().readText()

        server.expect(requestTo("/v1/chat/completions"))
            .andRespond(withSuccess(fixture, MediaType.APPLICATION_JSON))

        val response = client.chatCompletion(sampleRequest())

        assertThat(response.id).isEqualTo("chatcmpl-814")
        assertThat(response.objectType).isEqualTo("chat.completion")
        assertThat(response.model).isEqualTo("gemma4:12b")
        assertThat(response.systemFingerprint).isEqualTo("fp_ollama")
        assertThat(response.choices).hasSize(1)
        assertThat(response.choices[0].message.content).isEqualTo("Hello! How can I help you today?")
        assertThat(response.choices[0].finishReason).isEqualTo("stop")
        assertThat(response.usage.totalTokens).isEqualTo(107)
    }

    @Test
    fun `Ollama가 5xx를 주면 ProviderCallException`() {
        val(client, server) = clientWithServer()

        server.expect(requestTo("/v1/chat/completions"))
            .andRespond(withServerError())

        assertThrows<ProviderCallException> { client.chatCompletion(sampleRequest()) }
    }

    @Test
    fun `예상과 다른 응답 바디가 오면 ProviderCallException`() {
        val(client, server) = clientWithServer()

        server.expect(requestTo("/v1/chat/completions"))
            .andRespond(withSuccess("""{"예상치못한필드":"예상치못한값"}""", MediaType.APPLICATION_JSON))

        assertThrows<ProviderCallException> { client.chatCompletion(sampleRequest()) }
    }

    @Test
    fun `모델명을 덮어쓰지 않는다`() {
        val(client, server) = clientWithServer()
        val fixture = javaClass.getResourceAsStream("/ollama-sample-response.json")!!.bufferedReader().readText()

        server.expect(requestTo("/v1/chat/completions"))
            .andExpect(jsonPath("$.model").value("gemma4:26b"))
            .andRespond(withSuccess(fixture, MediaType.APPLICATION_JSON))

        client.chatCompletion(sampleRequest("gemma4:26b"))

        server.verify()
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

    private fun clientWithServer(): Pair<OpenAiCompatibleClient, MockRestServiceServer> {
        val builder = RestClient.builder()
        val server = MockRestServiceServer.bindTo(builder).build()
        val provider = Provider(
            baseUrl = "http://localhost",
            chatPath = "/v1/chat/completions",
            models = listOf("gemma4:12b", "gemma4:26b"),
            readTimeout = Duration.ofSeconds(2),
        )

        return OpenAiCompatibleClient("ollama", builder.build(), provider) to server
    }
}