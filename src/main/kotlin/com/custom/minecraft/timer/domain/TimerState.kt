package com.custom.minecraft.timer.domain

import org.bukkit.scheduler.BukkitTask

sealed class TimerState {
    object Idle : TimerState()
    data class Running(val task: BukkitTask) : TimerState()
    object Finished : TimerState()
}
