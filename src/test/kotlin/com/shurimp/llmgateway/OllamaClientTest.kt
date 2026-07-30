package com.shurimp.llmgateway

import com.shurimp.llmgateway.routing.api.ChatCompletionRequest
import com.shurimp.llmgateway.routing.api.Message
import com.shurimp.llmgateway.routing.domain.ProviderCallException
import com.shurimp.llmgateway.routing.infrastructure.OllamaClient
import com.shurimp.llmgateway.routing.infrastructure.OllamaProperties
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import java.time.Duration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertThrows
import org.springframework.test.web.client.response.MockRestResponseCreators.withServerError

class OllamaClientTest {

    @Test
    fun `실제 Ollama 응답 형태를 정상 파싱한다`() {
        val builder = RestClient.builder()
        val server = MockRestServiceServer.bindTo(builder).build()
        val client = OllamaClient(builder.build(), OllamaProperties("http://localhost", "gemma4:12b", Duration.ofSeconds(2)))

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
        val builder = RestClient.builder()
        val server = MockRestServiceServer.bindTo(builder).build()
        val client = OllamaClient(builder.build(), OllamaProperties("http://localhost", "gemma4:12b", Duration.ofSeconds(2)))

        server.expect(requestTo("/v1/chat/completions"))
            .andRespond(withServerError())

        assertThrows<ProviderCallException> { client.chatCompletion(sampleRequest()) }
    }

    @Test
    fun `예상과 다른 응답 바디가 오면 ProviderCallException`() {
        val builder = RestClient.builder()
        val server = MockRestServiceServer.bindTo(builder).build()
        val client = OllamaClient(builder.build(), OllamaProperties("http://localhost", "gemma4:12b", Duration.ofSeconds(2)))

        server.expect(requestTo("/v1/chat/completions"))
            .andRespond(withSuccess("""{"예상치못한필드":"예상치못한값"}""", MediaType.APPLICATION_JSON))

        assertThrows<ProviderCallException> { client.chatCompletion(sampleRequest()) }
    }

    private fun sampleRequest() = ChatCompletionRequest (
        model = "아무거나모델",
        messages = listOf(
            Message(
                role = "user",
                content = "hi"
            )
        ),
    )
}