package com.shurimp.llmgateway.eval.internal

data class EvalRunResultsResponse(
    val runId: Long,
    val cases: List<EvalResultCaseResponse>
)