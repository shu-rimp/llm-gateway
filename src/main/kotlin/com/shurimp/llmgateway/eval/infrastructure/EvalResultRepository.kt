package com.shurimp.llmgateway.eval.infrastructure

import com.shurimp.llmgateway.eval.domain.EvalResult
import org.springframework.data.jpa.repository.JpaRepository

interface EvalResultRepository: JpaRepository<EvalResult, Long> {

    fun findAllByRunIdOrderByCaseIdAsc(runId: Long): List<EvalResult>
}