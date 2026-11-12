package com.shurimp.llmgateway.eval.domain

import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "eval_results")
class EvalResult(
    val runId: Long,
    val caseId: Long,
    val answer: String? = null,
    val latencyMs: Int? = null,
    val containsPass: Boolean? = null,
    val judgeScore: Int? = null,
    val judgeReason: String? = null,
    val error: String? = null,
): BaseEntity()