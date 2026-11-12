package com.shurimp.llmgateway.eval.internal

import com.shurimp.llmgateway.eval.domain.EvalRun
import com.shurimp.llmgateway.eval.infrastructure.EvalRunRepository
import com.shurimp.llmgateway.routing.api.RoutingPort
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service

@Service
class EvalRunService(
    private val runRepository: EvalRunRepository,
    private val executor: EvalRunExecutor,
    private val routingPort: RoutingPort,
    private val properties: EvalProperties,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun startRun(targetModel: String): Long {
        if (!routingPort.supports(targetModel)) throw EvalException(EvalErrorCode.UNKNOWN_MODEL)

        val run = runRepository.save(EvalRun(targetModel, properties.judgeModel))
        val runId = run.id!!
        scope.launch { executor.execute(runId) }
        return runId
    }

    @PreDestroy
    fun shutdown() = scope.cancel()
}