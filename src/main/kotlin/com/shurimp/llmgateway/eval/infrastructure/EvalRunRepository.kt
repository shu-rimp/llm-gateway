package com.shurimp.llmgateway.eval.infrastructure

import com.shurimp.llmgateway.eval.domain.EvalRun
import org.springframework.data.jpa.repository.JpaRepository

interface EvalRunRepository: JpaRepository<EvalRun, Long>