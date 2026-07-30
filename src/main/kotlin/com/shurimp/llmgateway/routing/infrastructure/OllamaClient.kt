package com.shurimp.llmgateway.routing.infrastructure

import com.shurimp.llmgateway.routing.api.ChatCompletionRequest
import com.shurimp.llmgateway.routing.api.ChatCompletionResponse
import com.shurimp.llmgateway.routing.domain.ProviderCallException
import org.springframework.stereotype.Component
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient

@Component
class OllamaClient(
    private val ollamaRestClient: RestClient,
    private val properties: OllamaProperties,
) {

    fun chatCompletion(request: ChatCompletionRequest): ChatCompletionResponse {
        // "클라이언트는 모델을 몰라도 게이트웨이가 라우팅 한다."는 설계 의도에 따라 모델을 덮어씌움.
        // TODO: 나중에 여러 모델을 라우팅할 때 이 부분 변경 필요
        val ollamaRequest = request.copy(model = properties.model)

        return try {
            ollamaRestClient.post()
                .uri("/v1/chat/completions")
                .body(ollamaRequest)
                .retrieve()
                .onStatus({ it.isError }) { _, response -> // 응답이 왔는데 상태코드가 에러인 경우
                    throw ProviderCallException("failed to call ollama provider: HTTP ${response.statusCode}")
                }
                .requiredBody(ChatCompletionResponse::class.java)
        } catch (e: ResourceAccessException) { // 애초에 응답을 못 받은 경우
            throw ProviderCallException("failed to call ollama provider", e)
        }
    }
}