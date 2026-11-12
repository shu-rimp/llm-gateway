package com.shurimp.llmgateway

import com.shurimp.llmgateway.eval.internal.JudgeResponseParser
import com.shurimp.llmgateway.eval.internal.JudgeResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JudgeResponseParserTest {

    @Test
    fun `정상 JSON은 파싱 성공한다`() {
        val normalJson = """
            {
                "score": 2,
                "reason": "문장은 좋으나 답이 틀렸어요."
            }
        """.trimIndent()

        val result = JudgeResponseParser.parse(normalJson)
        assertThat(result.getOrNull()).isEqualTo(JudgeResult(score = 2, reason = "문장은 좋으나 답이 틀렸어요."))
    }

    @Test
    fun `코드블록으로 감싼 JSON은 파싱 성공한다`() {
        val codeBlockedJson = """
            ```json
            {
                "score": 2,
                "reason": "문장은 좋으나 답이 틀렸어요."
            }
            ```
        """.trimIndent()

        val result = JudgeResponseParser.parse(codeBlockedJson)
        assertThat(result.getOrNull()).isEqualTo(JudgeResult(score = 2, reason = "문장은 좋으나 답이 틀렸어요."))
    }

    @Test
    fun `앞 뒤에 설명이 붙은 JSON은 파싱 성공한다`() {
        val textAddedJson = """
            다음은 평가 결과입니다.
            {
                "score": 2,
                "reason": "문장은 좋으나 답이 틀렸어요."
            }
            평가를 마쳤습니다.
        """.trimIndent()

        val result = JudgeResponseParser.parse(textAddedJson)
        assertThat(result.getOrNull()).isEqualTo(JudgeResult(score = 2, reason = "문장은 좋으나 답이 틀렸어요."))
    }

    @Test
    fun `깨진 JSON 형식은 파싱 실패한다`() {
        val crackedJson = """
            {
                "score": 2,
                { "reason": "문장은 좋으나 답이 틀렸어요." }
        """.trimIndent()

        val result = JudgeResponseParser.parse(crackedJson)
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `reason 필드 누락시 파싱 실패한다`() {
        val omittedJson = """
            {
                "score": 2
            }
        """.trimIndent()

        val result = JudgeResponseParser.parse(omittedJson)
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `1-5 범위 밖 점수는 파싱 실패한다`() {
        val overScoredJson = """
            {
                "score": 6,
                "reason": "문장은 좋으나 답이 틀렸어요."
            }
        """.trimIndent()

        val result = JudgeResponseParser.parse(overScoredJson)
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `중괄호가 없는 응답은 파싱 실패한다`() {
        val notJson = """
            평가할 수 없습니다.
        """.trimIndent()

        val result = JudgeResponseParser.parse(notJson)
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `reason 안에 중활호가 있으면 파싱 실패한다`() {
        val weirdJson = """
            {
                "score": 2,
                "reason": "{문장은 좋으나 답이 틀렸어요.}"
            }
        """.trimIndent()

    }
}