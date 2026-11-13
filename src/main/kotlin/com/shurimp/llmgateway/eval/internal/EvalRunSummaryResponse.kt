package com.shurimp.llmgateway.eval.internal

import com.shurimp.llmgateway.eval.domain.EvalRunStatus
import java.time.Instant

data class EvalRunSummaryResponse(
    val runId: Long,
    val targetModel: String,
    val judgeModel: String,
    val status: EvalRunStatus,
    val startedAt: Instant?,
    val finishedAt: Instant?,
    val total: Int,
    val avgJudgeScore: Double?,
    val containsPassRate: Double?,
    val avgLatencyMs: Double?,
    val errorCount: Int,
)