package com.shurimp.llmgateway

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<LlmGatewayApplication>().with(TestcontainersConfiguration::class).run(*args)
}
