package com.shurimp.llmgateway

import com.shurimp.llmgateway.routing.api.ChatCompletionRequest
import com.shurimp.llmgateway.routing.api.ChatCompletionResponse
import com.shurimp.llmgateway.routing.api.Choice
import com.shurimp.llmgateway.routing.api.Message
import com.shurimp.llmgateway.routing.api.RoutingPort
import com.shurimp.llmgateway.routing.api.Usage

class FakeRoutingPort(
    private val judgeModel: String,
    private val judgeReply: String = """{"score": 5, "reason": "ok"}""",
    private val answerFor: (question: String) -> String,
): RoutingPort {
    val targetCalls = mutableListOf<ChatCompletionRequest>()
    val judgeCalls = mutableListOf<ChatCompletionRequest>()

    override fun routeChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse =
        if (request.model == judgeModel) {
            judgeCalls += request
            response(content = judgeReply)
        } else {
            targetCalls += request
            val answer = answerFor(request.messages.last().content)
            response(content = answer, finishReason = if (answer.isEmpty()) "length" else "stop")
        }

    override fun supports(model: String): Boolean = true

    private fun response(content: String, finishReason: String = "stop") =
        ChatCompletionResponse(
            objectType = "chat.completion",
            id = "fake",
            created = 0,
            model = "fake",
            choices = listOf(
                Choice(
                    index = 0,
                    message = Message(role = "assistant", content = content),
                    finishReason = finishReason,
                )
            ),
            usage = Usage(
                promptTokens = 0,
                completionTokens = 0,
                totalTokens = 0,
            ))
}