package com.shurimp.llmgateway.eval.internal

import com.shurimp.llmgateway.eval.infrastructure.EvalCaseRepository
import com.shurimp.llmgateway.eval.infrastructure.EvalResultRepository
import com.shurimp.llmgateway.eval.infrastructure.EvalRunRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class EvalRunQueryService(
    private val runRepository: EvalRunRepository,
    private val resultRepository: EvalResultRepository,
    private val caseRepository: EvalCaseRepository,
) {

    fun getSummary(runId: Long): EvalRunSummaryResponse {
        val run = runRepository.findByIdOrNull(runId)
            ?: throw EvalException(EvalErrorCode.RUN_NOT_FOUND)

        val results = resultRepository.findAllByRunIdOrderByCaseIdAsc(runId)

        return EvalRunSummary.summarize(run, results)
    }

    fun getResults(runId: Long): EvalRunResultsResponse {
        runRepository.findByIdOrNull(runId)
            ?: throw EvalException(EvalErrorCode.RUN_NOT_FOUND)

        val results = resultRepository.findAllByRunIdOrderByCaseIdAsc(runId)

        val casesById = caseRepository.findAllById(results.map { it.caseId })
            .associateBy { it.id }

        return EvalRunResultsResponse(
            runId = runId,
            cases = results.map { it.toResponse(casesById.getValue(it.caseId)) }
        )
    }
}
