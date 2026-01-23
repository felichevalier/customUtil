package com.custom.minecraft.timer.domain

sealed class TimerEvent {
    data class Start(val isSuccess: Boolean, val totalTime: Int = 0) : TimerEvent()
    data class Tick(val remainingTime: Int) : TimerEvent()
    object Pause: TimerEvent()
    object Resume: TimerEvent()
    object Finish : TimerEvent()
}