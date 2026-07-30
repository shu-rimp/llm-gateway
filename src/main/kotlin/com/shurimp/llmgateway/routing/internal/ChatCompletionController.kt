package com.shurimp.llmgateway.routing.internal

import com.shurimp.llmgateway.routing.api.ChatCompletionRequest
import com.shurimp.llmgateway.routing.api.ChatCompletionResponse
import com.shurimp.llmgateway.routing.api.RoutingPort
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ChatCompletionController(
    private val routingPort: RoutingPort,
) {

    @PostMapping("/v1/chat/completions")
    fun chatCompletions(@Valid @RequestBody request: ChatCompletionRequest): ChatCompletionResponse {
        if (request.stream) throw RoutingException(RoutingErrorCode.STREAMING_NOT_SUPPORTED)

        return routingPort.routeChatCompletion(request)
    }
}