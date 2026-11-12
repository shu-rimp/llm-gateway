package com.shurimp.llmgateway.eval.internal

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class EvalRunController(
    private val evalRunService: EvalRunService,
) {

    @PostMapping("/eval/runs")
    fun startRun(@Valid @RequestBody request: StartRunRequest): ResponseEntity<StartRunResponse> {
        val runId = evalRunService.startRun(request.targetModel)

        return ResponseEntity.accepted().body(StartRunResponse(runId))
    }
}