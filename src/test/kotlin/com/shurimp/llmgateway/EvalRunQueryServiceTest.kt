package com.shurimp.llmgateway

import com.shurimp.llmgateway.eval.domain.EvalCase
import com.shurimp.llmgateway.eval.domain.EvalResult
import com.shurimp.llmgateway.eval.domain.EvalRun
import com.shurimp.llmgateway.eval.infrastructure.EvalCaseRepository
import com.shurimp.llmgateway.eval.infrastructure.EvalResultRepository
import com.shurimp.llmgateway.eval.infrastructure.EvalRunRepository
import com.shurimp.llmgateway.eval.internal.EvalErrorCode
import com.shurimp.llmgateway.eval.internal.EvalException
import com.shurimp.llmgateway.eval.internal.EvalRunQueryService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(TestcontainersConfiguration::class)
class EvalRunQueryServiceTest(
    @Autowired private val runRepository: EvalRunRepository,
    @Autowired private val resultRepository: EvalResultRepository,
    @Autowired private val caseRepository: EvalCaseRepository,
) {

    private val service = EvalRunQueryService(runRepository, resultRepository, caseRepository)

    @Test
    fun `결과는 caseId 순으로 정렬되고 각 결과는 자신의 case와 짝지어진다`() {
        val caseAId =
            caseRepository.save(EvalCase(question = "질문 A", referenceAnswer = "모범답안 A")).id!!
        val caseBId =
            caseRepository.save(EvalCase(question = "질문 B", referenceAnswer = "모범답안 B")).id!!
        val runId =
            runRepository.save(EvalRun(targetModel = "gemma4:e2b", judgeModel = "judge")).id!!

        resultRepository.save(EvalResult(runId = runId, caseId = caseBId, answer = "B에 대한 답"))
        resultRepository.save(EvalResult(runId = runId, caseId = caseAId, answer = "A에 대한 답"))

        val response = service.getResults(runId)

        assertThat(response.cases.map { it.caseId }).containsExactly(caseAId, caseBId)
        assertThat(response.cases.map { it.question to it.answer })
            .containsExactly("질문 A" to "A에 대한 답", "질문 B" to "B에 대한 답")
    }

    @Test
    fun `없는 runId로 getResults를 수행하면 RUN_NOT_FOUND 에러가 난다`() {
        val unknownRunId = 999L
        val e = assertThrows<EvalException> { service.getResults(unknownRunId) }

        assertThat(e.errorCode).isEqualTo(EvalErrorCode.RUN_NOT_FOUND)
    }

    @Test
    fun `run은 있지만 결과가 0건이면 예외 없이 빈 리스트를 반환한다`() {
        val runId = runRepository.save(EvalRun(targetModel = "gemma4:e2b", judgeModel = "judge")).id!!

        val response = service.getResults(runId)

        assertThat(response.cases).isEmpty()
    }
}