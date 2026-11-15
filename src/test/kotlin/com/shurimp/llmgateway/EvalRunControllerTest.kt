package com.shurimp.llmgateway

import com.shurimp.llmgateway.eval.internal.EvalErrorCode
import com.shurimp.llmgateway.eval.internal.EvalException
import com.shurimp.llmgateway.eval.internal.EvalRunController
import com.shurimp.llmgateway.eval.internal.EvalRunQueryService
import com.shurimp.llmgateway.eval.internal.EvalRunService
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester

@WebMvcTest(EvalRunController::class)
class EvalRunControllerTest(
    @Autowired private val mvc: MockMvcTester
) {

    @MockitoBean lateinit var evalRunService: EvalRunService
    @MockitoBean lateinit var evalRunQueryService: EvalRunQueryService

    @Test
    fun `정상 POST 요청시 evalRunService에 위임되고 202를 반환한다`() {
        val targetModel = "gemma4:e2b"
        given(evalRunService.startRun(targetModel)).willReturn(1L)

        mvc.post().uri("/eval/runs")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"targetModel": "$targetModel"}""")
            .exchange()
            .assertThat()
            .hasStatus(HttpStatus.ACCEPTED)
            .bodyJson()
            .extractingPath("$.runId")
            .isEqualTo(1)
    }

    @Test
    fun `빈 모델로 POST 요청시 400 INVALID_REQUEST를 반환한다`() {
        mvc.post().uri("/eval/runs")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"targetModel": ""}""")
            .exchange()
            .assertThat()
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson()
            .extractingPath("$.error.code")
            .isEqualTo("INVALID_REQUEST")

        then(evalRunService).shouldHaveNoInteractions()
    }

    @Test
    fun `모르는 모델로 POST 요청시 400 UNKNOWN_MODEL을 반환한다`() {
        val unknownModel = "gpt-4o"
        given(evalRunService.startRun(unknownModel))
            .willThrow(EvalException(EvalErrorCode.UNKNOWN_MODEL))

        mvc.post().uri("/eval/runs")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"targetModel": "$unknownModel"}""")
            .exchange()
            .assertThat()
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson()
            .extractingPath("$.error.code")
            .isEqualTo("UNKNOWN_MODEL")
    }

    @Test
    fun `없는 runId로 GET 요청시 404를 반환한다`() {
        val unknownRunId = 999L
        given(evalRunQueryService.getSummary(unknownRunId))
            .willThrow(EvalException(EvalErrorCode.RUN_NOT_FOUND))

        mvc.get().uri("/eval/runs/$unknownRunId")
            .exchange()
            .assertThat()
            .hasStatus(HttpStatus.NOT_FOUND)
            .bodyJson()
            .extractingPath("$.error.type")
            .isEqualTo("NOT_FOUND_ERROR")
    }
}