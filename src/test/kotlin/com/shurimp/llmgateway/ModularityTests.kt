package com.shurimp.llmgateway

import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules

class ModularityTests {

    @Test
    fun verifiesModularStructure() {
        ApplicationModules.of(LlmGatewayApplication::class.java).verify()
    }
}