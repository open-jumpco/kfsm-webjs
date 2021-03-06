package com.example.kfsm

import com.example.kfsm.TurnstileEvent.COIN
import com.example.kfsm.TurnstileEvent.PASS
import com.example.kfsm.TurnstileState.LOCKED
import com.example.kfsm.TurnstileState.UNLOCKED
import io.jumpco.open.kfsm.stateMachine

data class TurnstileInfo(
    val locked: Boolean = true,
    val message: String = "",
    val alarm: Boolean = false
) {
    fun update(
        locked: Boolean? = null,
        message: String? = null,
        alarm: Boolean = false
    ): TurnstileInfo {
        return copy(locked ?: this.locked, message ?: "", alarm)
    }
}

enum class TurnstileEvent {
    COIN,
    PASS
}

enum class TurnstileState {
    LOCKED,
    UNLOCKED
}

class TurnstileFSM(turnstile: TurnstileInfo) {
    private val fsm = definition.create(turnstile)
    @JsName("event")
    fun event(event: String, info: TurnstileInfo) = fsm.sendEvent(TurnstileEvent.valueOf(event.toUpperCase()), info)
    @JsName("allowed")
    fun allowed(event: String) : Boolean {
        return fsm.allowed().map { it.name.toLowerCase() }.toSet().contains(event)
    }

    companion object {
        private val definition = stateMachine(
            TurnstileState.values().toSet(),
            TurnstileEvent.values().toSet(),
            TurnstileInfo::class,
            TurnstileInfo::class,
            TurnstileInfo::class
        ) {
            initialState { if (locked) LOCKED else UNLOCKED }
            default {
                action { _, _, info ->
                    require(info != null) { "Info required" }
                    info.update(message = "Alarm")
                }
            }
            whenState(LOCKED) {
                onEvent(COIN to UNLOCKED) { info ->
                    require(info != null) { "Info required" }
                    info.update(locked = false)
                }
            }
            whenState(UNLOCKED) {
                onEvent(PASS to LOCKED) { info ->
                    require(info != null) { "Info required" }
                    info.update(locked = true)
                }
                onEvent(COIN) { info ->
                    require(info != null) { "Info required" }
                    info.update(message = "Return Coin")
                }
            }
        }.build()
    }
}
