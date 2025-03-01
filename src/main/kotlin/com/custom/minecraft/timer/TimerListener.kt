package com.custom.minecraft.timer

interface TimerListener {

    fun onStartTimer(isSuccess: Boolean) {}

    fun onResumeTimer() {}

    fun onUpdateTimer(time: Int) {}

    fun onPauseTimer() {}

    fun onFinishTimer() {}
}