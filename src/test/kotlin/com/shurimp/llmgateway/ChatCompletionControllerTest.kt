package com.shurimp.llmgateway

import com.shurimp.llmgateway.routing.api.ChatCompletionRequest
import com.shurimp.llmgateway.routing.api.ChatCompletionResponse
import com.shurimp.llmgateway.routing.api.Choice
import com.shurimp.llmgateway.routing.api.Message
import com.shurimp.llmgateway.routing.api.RoutingPort
import com.shurimp.llmgateway.routing.api.Usage
import com.shurimp.llmgateway.routing.domain.ProviderCallException
import com.shurimp.llmgateway.routing.internal.ChatCompletionController
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester

@WebMvcTest(ChatCompletionController::class)
class ChatCompletionControllerTest(@Autowired private val mvc: MockMvcTester) {

    @MockitoBean
    lateinit var routingPort: RoutingPort

    @Test
    fun `정상 요청은 RoutingPort에 위임되고 200을 반환`() {
        given(routingPort.routeChatCompletion(sampleRequest())).willReturn(sampleResponse())

        mvc.post().uri("/v1/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"model": "gemma4:12b", "messages": [{"role": "user", "content": "hi"}]}""")
            .exchange()
            .assertThat()
            .hasStatus(HttpStatus.OK)
    }

    @Test
    fun `stream=true는 400과 에러바디를 반환`() {
        mvc.post().uri("/v1/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"model": "gemma4:12b", "messages": [{"role": "user", "content": "hi"}], "stream": true}""")
            .exchange()
            .assertThat()
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson().extractingPath("$.error.code").isEqualTo("STREAMING_NOT_SUPPORTED")
    }

    @Test
    fun `빈 messages는 400을 반환`() {
        mvc.post().uri("/v1/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"model": "gemma4:12b", "messages": []}""")
            .exchange()
            .assertThat()
            .hasStatus(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `RoutingPort가 ProviderCallException을 던지면 502를 반환`() {
        given(routingPort.routeChatCompletion(sampleRequest())).willThrow(ProviderCallException("으악"))

        mvc.post().uri("/v1/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"model": "gemma4:12b", "messages": [{"role": "user", "content": "hi"}]}""")
            .exchange()
            .assertThat()
            .hasStatus(HttpStatus.BAD_GATEWAY)
            .bodyJson().extractingPath("$.error.code").isEqualTo("PROVIDER_ERROR")
    }

    private fun sampleResponse() = ChatCompletionResponse(
        objectType = "chat.completion",
        id = "chatcmpl-test",
        created = 0,
        model = "gemma4:12b",
        choices = listOf(
            Choice(
                index = 0,
                message = Message(role = "assistant", content = "hi"),
                finishReason = "finish",
            )
        ),
        usage = Usage(
            promptTokens = 1,
            completionTokens = 1,
            totalTokens = 2,
        )
    )

    private fun sampleRequest() = ChatCompletionRequest (
        model = "gemma4:12b",
        messages = listOf(
            Message(
                role = "user",
                content = "hi"
            )
        ),
    )
}