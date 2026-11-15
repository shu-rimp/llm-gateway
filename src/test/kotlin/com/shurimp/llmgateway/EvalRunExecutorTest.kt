package com.shurimp.llmgateway

import com.shurimp.llmgateway.eval.domain.EvalCase
import com.shurimp.llmgateway.eval.domain.EvalRun
import com.shurimp.llmgateway.eval.domain.EvalRunStatus
import com.shurimp.llmgateway.eval.infrastructure.EvalCaseRepository
import com.shurimp.llmgateway.eval.infrastructure.EvalResultRepository
import com.shurimp.llmgateway.eval.infrastructure.EvalRunRepository
import com.shurimp.llmgateway.eval.internal.EvalProperties
import com.shurimp.llmgateway.eval.internal.EvalRunExecutor
import com.shurimp.llmgateway.eval.internal.JudgeService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull

@DataJpaTest
@Import(TestcontainersConfiguration::class)
class EvalRunExecutorTest(
    @Autowired private val runRepository: EvalRunRepository,
    @Autowired private val caseRepository: EvalCaseRepository,
    @Autowired private val resultRepository: EvalResultRepository,
) {

    private val properties = EvalProperties(judgeModel = "judge", maxTokens = 100)

    @BeforeEach
    fun setUp() {
        caseRepository.deleteAll()
    }

    @Test
    fun `모든 케이스가 성공하면 모두 채점되고 run은 COMPLETED가 된다`() {
        // given: 케이스 A, B 저장 + run 저장, port 생성(모두 정답)
        caseRepository.save(EvalCase(question = "질문 A", referenceAnswer = "모범답안 A"))
        caseRepository.save(EvalCase(question = "질문 B", referenceAnswer = "모범답안 B"))
        val runId =
            runRepository.save(EvalRun(targetModel = "gemma4:e2b", judgeModel = "judge")).id!!
        
        val port = FakeRoutingPort("judge") { "정답" }

        // when
        executorWith(port).execute(runId)

        // then
        // 1. run은 COMPLETED 상태를 가진다
        val run = runRepository.findByIdOrNull(runId)!!
        assertThat(run.status).isEqualTo(EvalRunStatus.COMPLETED)
        assertThat(run.startedAt).isNotNull
        assertThat(run.finishedAt).isNotNull

        // 2. 모든 케이스의 결과가 정상이다
        val results = resultRepository.findAllByRunIdOrderByCaseIdAsc(runId)
        assertThat(results).hasSize(2)
        assertThat(results.map { it.judgeScore }).containsOnly(5)
        assertThat(results.map { it.answer }).containsOnly("정답")
        assertThat(results.map { it.error }).containsOnly(null)
        assertThat(results.map { it.latencyMs }).doesNotContainNull()

        // 3. judge는 총 2회 호출된다
        assertThat(port.judgeCalls).hasSize(2)
        assertThat(port.targetCalls.map { it.maxTokens }).containsOnly(100)
    }

    @Test
    fun `케이스 하나가 실패해도 나머지 결과는 저장되고 run은 COMPLETED가 된다`() {
        // given: 케이스 A, B 저장 + run 저장, port 생성(하나만 에러)
        val caseAId =
            caseRepository.save(EvalCase(question = "질문 A", referenceAnswer = "모범답안 A")).id!!
        val caseBId =
            caseRepository.save(EvalCase(question = "질문 B", referenceAnswer = "모범답안 B")).id!!
        val runId =
            runRepository.save(EvalRun(targetModel = "gemma4:e2b", judgeModel = "judge")).id!!

        val port = FakeRoutingPort("judge") { question ->
            if (question == "질문 B") throw RuntimeException("read timeout")
            else "정답"
        }

        // when
        executorWith(port).execute(runId)

        // then
        // 1. run은 COMPLETED 상태를 가진다
        val run = runRepository.findByIdOrNull(runId)!!
        assertThat(run.status).isEqualTo(EvalRunStatus.COMPLETED)
        assertThat(run.finishedAt).isNotNull

        // 2. 정상 케이스는 정상 결과가, 에러 케이스는 에러가 저장된다
        val results = resultRepository.findAllByRunIdOrderByCaseIdAsc(runId)
        assertThat(results).hasSize(2)

        val cases = results.associateBy { it.caseId }
        val resultA = cases.getValue(caseAId)
        assertThat(resultA.answer).isEqualTo("정답")
        assertThat(resultA.judgeScore).isEqualTo(5)
        assertThat(resultA.error).isNull()

        val resultB = cases.getValue(caseBId)
        assertThat(resultB.error).contains("read timeout")
        assertThat(resultB.answer).isNull()
        assertThat(resultB.judgeScore).isNull()

        // 3. judge는 에러를 제외한 1회만 호출된다
        assertThat(port.judgeCalls).hasSize(1)
    }

    @Test
    fun `빈 답변인 경우 error에 기록하고 answer는 null, latencyMs는 기록한다`() {
        // given: 케이스 저장, run 저장, port 생성(빈 답변)
        caseRepository.save(EvalCase(question = "질문", referenceAnswer = "모범 답안"))
        val runId = runRepository.save(EvalRun(targetModel = "gemma4:e2b", judgeModel = "judge")).id!!

        val port = FakeRoutingPort("judge") { "" }
        
        // when
        executorWith(port).execute(runId)
        
        // then
        // 1. run은 COMPLETED 상태를 가진다
        val run = runRepository.findByIdOrNull(runId)!!
        assertThat(run.status).isEqualTo(EvalRunStatus.COMPLETED)
        
        // 2. error와 latencyMs는 기록되고 answer는 null이 저장된다
        val result = resultRepository.findAllByRunIdOrderByCaseIdAsc(runId).single()
        assertThat(result.error).contains("finish_reason=length")
        assertThat(result.latencyMs).isNotNull
        assertThat(result.answer).isNull()
        
        // 3. judge는 호출되지 않는다
        assertThat(port.targetCalls).hasSize(1)
        assertThat(port.judgeCalls).isEmpty()
    }

    @Test
    fun `응답에서 json 파싱에 실패하면 answer는 기록, judgeScore는 null, error에 기록한다`() {
        // given: 케이스 저장, run 저장, port 생성(Not JSON)
        caseRepository.save(EvalCase(question = "질문", referenceAnswer = "모범 답안"))
        val runId = runRepository.save(EvalRun(targetModel = "gemma4:e2b", judgeModel = "judge")).id!!

        val port = FakeRoutingPort("judge", judgeReply = "NOT JSON") { "정답" }

        // when
        executorWith(port).execute(runId)
        
        // then
        // 1. run은 COMPLETED 상태를 가진다
        val run = runRepository.findByIdOrNull(runId)!!
        assertThat(run.status).isEqualTo(EvalRunStatus.COMPLETED)
        
        // 2. answer와 error는 기록, judgeScore는 null이 저장된다
        val result = resultRepository.findAllByRunIdOrderByCaseIdAsc(runId).single()
        assertThat(result.answer).isEqualTo("정답")
        assertThat(result.error).startsWith("judge failed")
        assertThat(result.judgeScore).isNull()

        // 3. judge는 총 3번(1번 + 재시도 2번) 호출된다
        assertThat(port.targetCalls).hasSize(1)
        assertThat(port.judgeCalls).hasSize(3)
    }

    private fun executorWith(port: FakeRoutingPort) = EvalRunExecutor(
        runRepository,
        caseRepository,
        resultRepository,
        port,
        JudgeService(port, properties),
        properties,
    )
}