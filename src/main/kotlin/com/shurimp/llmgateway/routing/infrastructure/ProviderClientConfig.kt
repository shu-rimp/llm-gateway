package com.shurimp.llmgateway.routing.infrastructure

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.time.Duration

@Configuration
class ProviderClientConfig {

    @Bean
    fun providerClients(properties: ProviderProperties): ProviderClients =
        ProviderClients(
            properties.providers.map { (name, provider) ->
                OpenAiCompatibleClient(name, buildRestClient(provider), provider)
            }
        )

    private fun buildRestClient(provider: Provider): RestClient {
        val requestFactory = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(Duration.ofSeconds(2))
            setReadTimeout(provider.readTimeout)
        }

        return RestClient.builder()
            .baseUrl(provider.baseUrl)
            .requestFactory(requestFactory)
            .apply { builder ->
                provider.apiKey
                    ?.takeIf { it.isNotBlank() }
                    ?.let { key -> builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $key") }
            }
            .build()
    }
}