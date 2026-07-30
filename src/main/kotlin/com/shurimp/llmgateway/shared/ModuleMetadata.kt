package com.shurimp.llmgateway.shared

import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.PackageInfo

// 도메인 로직/상태와 전혀 관계없는 공통 모듈
@ApplicationModule(type = ApplicationModule.Type.OPEN)
@PackageInfo
class ModuleMetadata {
}