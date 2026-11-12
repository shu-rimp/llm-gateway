package com.shurimp.llmgateway.eval.infrastructure

import com.shurimp.llmgateway.eval.domain.EvalCase
import org.springframework.data.jpa.repository.JpaRepository

interface EvalCaseRepository: JpaRepository<EvalCase, Long>