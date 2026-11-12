package com.shurimp.llmgateway.eval.domain

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "eval_runs")
class EvalRun(
    val targetModel: String,
    val judgeModel: String,
): BaseEntity() {
    @Enumerated(EnumType.STRING)
    var status: EvalRunStatus = EvalRunStatus.PENDING
        protected set
    var startedAt: Instant? = null
        protected set
    var finishedAt: Instant? = null
        protected set

    fun start() {
        status = EvalRunStatus.RUNNING
        startedAt = Instant.now()
    }

    fun complete() {
        status = EvalRunStatus.COMPLETED
        finishedAt = Instant.now()
    }

    fun fail() {
        status = EvalRunStatus.FAILED
        finishedAt = Instant.now()
    }
}