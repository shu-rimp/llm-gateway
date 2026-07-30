package com.shurimp.llmgateway.routing.internal

import com.shurimp.llmgateway.routing.api.ChatCompletionRequest
import com.shurimp.llmgateway.routing.api.ChatCompletionResponse
import com.shurimp.llmgateway.routing.api.RoutingPort
import com.shurimp.llmgateway.routing.infrastructure.OllamaClient
import org.springframework.stereotype.Service

@Service
class RoutingService(
    private val ollamaClient: OllamaClient,
) : RoutingPort {
    override fun routeChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse {
        return ollamaClient.chatCompletion(request)
    }
}