package com.shurimp.llmgateway.eval.internal

import com.shurimp.llmgateway.eval.domain.EvalCase
import com.shurimp.llmgateway.eval.domain.EvalResult

data class EvalResultCaseResponse(
    val caseId: Long,
    val question: String,
    val referenceAnswer: String,
    val mustContain: String? = null,
    val answer: String? = null,
    val latencyMs: Int? = null,
    val containsPass: Boolean? = null,
    val judgeScore: Int? = null,
    val judgeReason: String? = null,
    val error: String? = null,
)

fun EvalResult.toResponse(case: EvalCase): EvalResultCaseResponse =
    EvalResultCaseResponse(
        caseId = this.caseId,
        question = case.question,
        referenceAnswer = case.referenceAnswer,
        mustContain = case.mustContain,
        answer = this.answer,
        latencyMs = this.latencyMs,
        containsPass = this.containsPass,
        judgeScore = this.judgeScore,
        judgeReason = this.judgeReason,
        error = this.error,
    )
