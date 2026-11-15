package com.shurimp.llmgateway

import com.shurimp.llmgateway.eval.domain.EvalResult
import com.shurimp.llmgateway.eval.domain.EvalRun
import com.shurimp.llmgateway.eval.internal.EvalRunSummary
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils

class EvalRunSummaryTest {

    // db가 없으므로 id를 생성할 수 없어 테스트전용 유틸 도구로 주입한다
    private val run = EvalRun(targetModel = "gemma4:e2b", judgeModel = "judge")
        .also { ReflectionTestUtils.setField(it, "id", 1L) }

    @Test
    fun `결과가 0건이면 avgJudgeScore, containsPassRate, avgLatencyMs는 Null, errorCount는 0을 반환한다`() {
        val results = emptyList<EvalResult>()
        val summary = EvalRunSummary.summarize(run, results)

        assertThat(summary.avgJudgeScore).isNull()
        assertThat(summary.avgLatencyMs).isNull()
        assertThat(summary.containsPassRate).isNull()
        assertThat(summary.errorCount).isZero
    }

    @Test
    fun `에러 케이스는 평균 집계에서 제외되고 errorCount로 집계된다`() {
        val results = listOf(
            EvalResult(runId = 1L, caseId = 1L, answer = "a", latencyMs = 100, judgeScore = 5),
            EvalResult(runId = 1L, caseId = 2L, answer = "b", latencyMs = 200, judgeScore = 3),
            EvalResult(runId = 1L, caseId = 3L, error = "timeout"),
        )

        val summary = EvalRunSummary.summarize(run, results)

        assertThat(summary.total).isEqualTo(3)
        assertThat(summary.avgJudgeScore).isEqualTo(4.0)
        assertThat(summary.avgLatencyMs).isEqualTo(150.0)
        assertThat(summary.errorCount).isEqualTo(1)
    }

    @Test
    fun `containsPass가 true, false, null이면 null 케이스를 제외한 비율이 나온다`() {
        val results = listOf(
            EvalResult(runId = 1L, caseId = 1L, answer = "a", containsPass = true, latencyMs = 100, judgeScore = 5),
            EvalResult(runId = 1L, caseId = 2L, answer = "b", containsPass = false, latencyMs = 100, judgeScore = 5),
            EvalResult(runId = 1L, caseId = 3L, answer = "c", containsPass = null, latencyMs = 100, judgeScore = 5),
        )

        val summary = EvalRunSummary.summarize(run, results)

        assertThat(summary.containsPassRate).isEqualTo(0.5)
    }

    @Test
    fun `전부 에러가 나면 평균은 모두 null이고 errorCount는 결과 수와 같다`() {
        val results = listOf(
            EvalResult(runId = 1L, caseId = 1L, error = "timeout"),
            EvalResult(runId = 1L, caseId = 2L, error = "timeout"),
            EvalResult(runId = 1L, caseId = 3L, error = "timeout"),
        )

        val summary = EvalRunSummary.summarize(run, results)

        assertThat(summary.avgLatencyMs).isNull()
        assertThat(summary.avgJudgeScore).isNull()
        assertThat(summary.containsPassRate).isNull()
        assertThat(summary.errorCount).isEqualTo(results.size)
    }
}