package com.shurimp.llmgateway.eval.internal

import com.shurimp.llmgateway.eval.domain.EvalResult
import com.shurimp.llmgateway.eval.domain.EvalRun

object EvalRunSummary {
    fun summarize(run: EvalRun, results: List<EvalResult>): EvalRunSummaryResponse {
        val avgJudgeScore = results.mapNotNull { it.judgeScore }
            .takeIf { it.isNotEmpty() }?.average()
        val containsPassRate = results.mapNotNull { it.containsPass }
            .takeIf { it.isNotEmpty() }
            ?.let { checks -> checks.count { it }.toDouble() / checks.size }
        val avgLatencyMs = results.mapNotNull { it.latencyMs }
            .takeIf { it.isNotEmpty() }?.average()
        val errorCount = results.count { it.error != null }

        return EvalRunSummaryResponse(
            runId = run.id!!,
            targetModel = run.targetModel,
            judgeModel = run.judgeModel,
            status = run.status,
            startedAt = run.startedAt,
            finishedAt = run.finishedAt,
            total = results.size,
            avgJudgeScore = avgJudgeScore,
            containsPassRate = containsPassRate,
            avgLatencyMs = avgLatencyMs,
            errorCount = errorCount,
        )
    }
}