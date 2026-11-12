package com.shurimp.llmgateway.eval.internal

import com.shurimp.llmgateway.eval.domain.EvalCase
import com.shurimp.llmgateway.eval.domain.EvalResult
import com.shurimp.llmgateway.eval.domain.EvalRun
import com.shurimp.llmgateway.eval.infrastructure.EvalCaseRepository
import com.shurimp.llmgateway.eval.infrastructure.EvalResultRepository
import com.shurimp.llmgateway.eval.infrastructure.EvalRunRepository
import com.shurimp.llmgateway.routing.api.ChatCompletionRequest
import com.shurimp.llmgateway.routing.api.Message
import com.shurimp.llmgateway.routing.api.RoutingPort
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import kotlin.time.measureTimedValue

@Component
class EvalRunExecutor(
    private val runRepository: EvalRunRepository,
    private val caseRepository: EvalCaseRepository,
    private val resultRepository: EvalResultRepository,
    private val routingPort: RoutingPort,
    private val judgeService: JudgeService,
    private val properties: EvalProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun execute(runId: Long) {
        val run = runRepository.findByIdOrNull(runId)
            ?: return log.warn("run ID: {} does not exist", runId)

        run.start()
        runRepository.save(run)

        try {
            caseRepository.findAll(Sort.by("id"))
                .forEach { case -> resultRepository.save(runCase(run, case)) }

            run.complete()
        } catch (e: Exception) {
            log.error("Error occurred while running. run ID: {}", runId, e)
            run.fail()
        }
        runRepository.save(run)
    }

    private fun runCase(run: EvalRun, case: EvalCase): EvalResult {
        val runId = run.id!!
        val caseId = case.id!!

        return runCatching {
            val (response, elapsed) = measureTimedValue {
                routingPort.routeChatCompletion(
                    ChatCompletionRequest(
                        model = run.targetModel,
                        messages = listOf(
                            Message(
                                role = "user",
                                content = case.question,
                            )
                        ),
                        temperature = 0.0,
                        maxTokens = properties.maxTokens,
                    )
                )
            }

            val choice = response.choices.firstOrNull()
            val answer = choice?.message?.content?.takeIf { it.isNotBlank() }
                ?: return EvalResult(
                    runId = runId,
                    caseId = caseId,
                    latencyMs = elapsed.inWholeMilliseconds.toInt(),
                    error = "empty answer (finish_reason=${choice?.finishReason})",
                )

            val containsPass = case.mustContain?.let { keyword ->
                answer.contains(keyword, ignoreCase = true)
            }
            val result = judgeService.judge(case, answer)
            result.exceptionOrNull()?.let { log.warn("judge failed for case ID {} in run ID {}", caseId, runId, it) }

            EvalResult(
                runId = runId,
                caseId = caseId,
                answer = answer,
                latencyMs = elapsed.inWholeMilliseconds.toInt(),
                containsPass = containsPass,
                judgeScore = result.getOrNull()?.score,
                judgeReason = result.getOrNull()?.reason,
                error = result.exceptionOrNull()?.let { "judge failed: ${it.message}" }
            )
        }.getOrElse { e ->
            log.warn("case ID {} failed in run ID {}", caseId, runId, e)
            EvalResult(
                runId = runId,
                caseId = caseId,
                error = e.message ?: e::class.simpleName
            )
        }
    }
}