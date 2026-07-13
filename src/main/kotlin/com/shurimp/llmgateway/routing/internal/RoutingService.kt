package com.shurimp.llmgateway.routing.internal

import com.shurimp.llmgateway.routing.api.ChatCompletionRequest
import com.shurimp.llmgateway.routing.api.ChatCompletionResponse
import com.shurimp.llmgateway.routing.api.RoutingPort

class RoutingService : RoutingPort {
    override fun routeChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse {
        TODO("Ollama 라우팅 로직")
    }
}