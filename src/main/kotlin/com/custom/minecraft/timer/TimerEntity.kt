package com.custom.minecraft.timer

import org.bukkit.entity.Player

data class TimerEntity(
    var timerUnit: TimerUnit = TimerUnit.SECOND,
    var time: Int = 0,
    val targetPlayers: MutableList<Player> = mutableListOf(),
    val isTimerMessageEnabled: Boolean = true,
    val startMessage: String = "スタート",
    val finishMessage: String = "終了"
)