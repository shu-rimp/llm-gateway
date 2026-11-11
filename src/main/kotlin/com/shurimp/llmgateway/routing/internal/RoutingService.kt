package com.shurimp.llmgateway.routing.internal

import com.shurimp.llmgateway.routing.api.ChatCompletionRequest
import com.shurimp.llmgateway.routing.api.ChatCompletionResponse
import com.shurimp.llmgateway.routing.api.RoutingPort
import com.shurimp.llmgateway.routing.infrastructure.ProviderClients
import org.springframework.stereotype.Service

@Service
class RoutingService(
    private val providerClients: ProviderClients,
) : RoutingPort {
    override fun routeChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse {
        val client = providerClients.forModel(request.model)
            ?: throw RoutingException(RoutingErrorCode.UNKNOWN_MODEL)

        return client.chatCompletion(request)
    }
}