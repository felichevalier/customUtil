package com.custom.minecraft.timer.domain

sealed class TimerEvent {
    data class Started(
        val timerId: String,
        val totalSeconds: Int,
    ) : TimerEvent()

    data class Tick(
        val timerId: String,
        val remainingSeconds: Int,
    ) : TimerEvent()

    data class Finished(
        val timerId: String,
        val reason: FinishReason,
    ) : TimerEvent()
}
