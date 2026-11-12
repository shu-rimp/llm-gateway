package com.shurimp.llmgateway.eval.internal

import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue

object JudgeResponseParser {
    private val mapper = jacksonObjectMapper()

    fun parse(raw: String): Result<JudgeResult> =
        runCatching {
            val start = raw.indexOf('{')
            val end = raw.lastIndexOf('}')
            require(start != -1 && end > start) { "no JSON object in judge response" }

            val result = mapper.readValue<JudgeResult>(raw.substring(start, end + 1))
            require(result.score in 1..5) { "judge score out of range: ${result.score}" }

            result
        }
}