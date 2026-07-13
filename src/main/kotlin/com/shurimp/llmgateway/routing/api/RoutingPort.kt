package com.shurimp.llmgateway.routing.api

interface RoutingPort {

    fun routeChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse

}