package com.whistlehub.common.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LogoutManager는 애플리케이션 전역에서 로그아웃 이벤트를 관리하기 위한 클래스입니다.
 *
 * 주요 특징:
 * - MutableSharedFlow를 사용하여 로그아웃 이벤트를 방송(broadcast) 합니다.
 * - 로그아웃 이벤트는 Unit 값을 emit 하며, 구독자들은 이 이벤트를 수신하여
 * - @Singleton 애노테이션을 사용해 앱 전체에서 하나의 인스턴스가 생성되도록 보장합니다.
 * - Dagger/Hilt를 통해 의존성 주입으로 사용되므로, 다른 ViewModel이나 클래스에서 쉽게 주입받아 사용할 수 있습니다.
 *
 * 사용 방법:
 * - 로그아웃이 필요한 상황(예, 토큰 만료, 사용자 직접 로그아웃 등)에서 [emitLogout] 함수를 호출합니다.
 * - UI 계층에서 [logoutEventFlow]를 구독하여,
 *   로그아웃 이벤트가 발생했을 때 로그인 화면으로 전환하는 등의 처리를 수행합니다.
 */

@Singleton
class LogoutManager @Inject constructor() {
    // 내부에서 로그아웃 이벤트를 발행하는 MutableSharedFlow. 외부에는 읽기 전용 SharedFlow로 노출합니다.
    private val _logoutEventFlow = MutableSharedFlow<Unit>()
    // 로그아웃 이벤트를 구독할 수 있도록 SharedFlow 형태로 공개합니다.
    val logoutEventFlow: SharedFlow<Unit> = _logoutEventFlow

    // 이 함수는 suspend 함수이므로 코루틴 내부에서 호출되어야 합니다.
    suspend fun emitLogout() {
        _logoutEventFlow.emit(Unit)
    }
}
