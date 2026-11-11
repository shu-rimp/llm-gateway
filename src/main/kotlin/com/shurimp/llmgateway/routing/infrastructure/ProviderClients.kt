package com.shurimp.llmgateway.routing.infrastructure

class ProviderClients(
    private val clients: List<OpenAiCompatibleClient>
) {
    fun forModel(model: String): OpenAiCompatibleClient? =
        clients.firstOrNull { it.supports(model) }
}
