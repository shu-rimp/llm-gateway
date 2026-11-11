package com.shurimp.llmgateway

import com.shurimp.llmgateway.routing.api.ChatCompletionRequest
import com.shurimp.llmgateway.routing.api.ChatCompletionResponse
import com.shurimp.llmgateway.routing.api.Choice
import com.shurimp.llmgateway.routing.api.Message
import com.shurimp.llmgateway.routing.api.RoutingPort
import com.shurimp.llmgateway.routing.api.Usage
import com.shurimp.llmgateway.routing.domain.ProviderCallException
import com.shurimp.llmgateway.routing.internal.ChatCompletionController
import com.shurimp.llmgateway.routing.internal.RoutingErrorCode
import com.shurimp.llmgateway.routing.internal.RoutingException
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
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

    @Test
    fun `지원하지 않는 모델이면 400과 UNKNOWN_MODEL을 반환한다`() {
        given(routingPort.routeChatCompletion(sampleRequest("gpt-4o")))
            .willThrow(RoutingException(RoutingErrorCode.UNKNOWN_MODEL))

        mvc.post().uri("/v1/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"model": "gpt-4o", "messages": [{"role": "user", "content": "hi"}]}""")
            .exchange()
            .assertThat()
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson().extractingPath("$.error.code").isEqualTo("UNKNOWN_MODEL")
    }

    @Test
    fun `max_tokens 값이 0 이하면 INVALID_REQUEST를 반환한다`() {
        mvc.post().uri("/v1/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"model": "gemma4:12b", "messages": [{"role": "user", "content": "hi"}], "max_tokens": 0}""")
            .exchange()
            .assertThat()
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson().extractingPath("$.error.code").isEqualTo("INVALID_REQUEST")

        then(routingPort).shouldHaveNoMoreInteractions()
    }

    @Test
    fun `허용되지 않은 role이면 400과 INVALID_REQUEST를 반환한다`() {
        mvc.post().uri("/v1/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"model": "gemma4:12b", "messages": [{"role": "이상한역할", "content": "hi"}]}""")
            .exchange()
            .assertThat()
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson().extractingPath("$.error.code").isEqualTo("INVALID_REQUEST")

        then(routingPort).shouldHaveNoMoreInteractions()
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

    private fun sampleRequest(model: String = "gemma4:12b") = ChatCompletionRequest (
        model = model,
        messages = listOf(
            Message(
                role = "user",
                content = "hi"
            )
        ),
    )
}