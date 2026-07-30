package com.shurimp.llmgateway

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
	
// TODO: kafka 구현 단계에서 살릴 것
//	@Bean
//	@ServiceConnection
//	fun kafkaContainer(): KafkaContainer {
//		return KafkaContainer(DockerImageName.parse("apache/kafka-native:latest"))
//	}

	@Bean
	@ServiceConnection
	fun postgresContainer(): PostgreSQLContainer {
		return PostgreSQLContainer(DockerImageName.parse("timescale/timescaledb:2.28.3-pg17")
			.asCompatibleSubstituteFor("postgres"))
	}

}
