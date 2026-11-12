package com.shurimp.llmgateway.eval.domain

import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "eval_cases")
class EvalCase(
    val question: String,
    val referenceAnswer: String,
    val mustContain: String? = null,
): BaseEntity()