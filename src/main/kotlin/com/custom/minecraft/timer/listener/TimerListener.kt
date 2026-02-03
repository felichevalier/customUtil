package com.custom.minecraft.timer.listener

import com.custom.minecraft.timer.domain.TimerEvent

/**
 * タイマーの状態変化を外部へ通知するためのリスナー。
 */
interface TimerListener {
    fun onEvent(event: TimerEvent)
}