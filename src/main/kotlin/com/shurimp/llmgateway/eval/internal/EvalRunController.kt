package com.shurimp.llmgateway.eval.internal

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class EvalRunController(
    private val evalRunService: EvalRunService,
    private val evalRunQueryService: EvalRunQueryService,
) {

    @PostMapping("/eval/runs")
    fun startRun(@Valid @RequestBody request: StartRunRequest): ResponseEntity<StartRunResponse> {
        val runId = evalRunService.startRun(request.targetModel)

        return ResponseEntity.accepted().body(StartRunResponse(runId))
    }

    @GetMapping("/eval/runs/{id}")
    fun getRun(@PathVariable id: Long): EvalRunSummaryResponse =
        evalRunQueryService.getSummary(id)

    @GetMapping("/eval/runs/{id}/results")
    fun getRunResults(@PathVariable id: Long): EvalRunResultsResponse =
        evalRunQueryService.getResults(id)
}