package com.shurimp.llmgateway.eval.internal

class EvalException(val errorCode: EvalErrorCode): RuntimeException(errorCode.message)