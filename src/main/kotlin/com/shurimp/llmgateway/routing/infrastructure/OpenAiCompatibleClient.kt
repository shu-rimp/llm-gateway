package com.shurimp.llmgateway.routing.infrastructure

import com.shurimp.llmgateway.routing.api.ChatCompletionRequest
import com.shurimp.llmgateway.routing.api.ChatCompletionResponse
import com.shurimp.llmgateway.routing.domain.ProviderCallException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

class OpenAiCompatibleClient(
    val name: String,
    private val restClient: RestClient,
    private val provider: Provider,
) {

    fun chatCompletion(request: ChatCompletionRequest): ChatCompletionResponse {
        return try {
            restClient.post()
                .uri(provider.chatPath)
                .body(request)
                .retrieve()
                .onStatus({ it.isError }) { _, response -> // 응답이 왔는데 상태코드가 에러인 경우
                    throw ProviderCallException("failed to call $name provider: HTTP ${response.statusCode}")
                }
                .requiredBody(ChatCompletionResponse::class.java)
        } catch (e: RestClientException) { // 애초에 응답을 못 받은 경우
            throw ProviderCallException("failed to call $name provider", e)
        }
    }

    fun supports(model: String): Boolean = model in provider.models
}