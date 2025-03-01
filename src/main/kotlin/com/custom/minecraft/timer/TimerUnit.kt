package com.custom.minecraft.timer

import com.custom.minecraft.command.TimerCommandConstants.HOUR_UNIT
import com.custom.minecraft.command.TimerCommandConstants.MINUTE_UNIT
import com.custom.minecraft.command.TimerCommandConstants.SECOND_UNIT

enum class TimerUnit(private val unit: String, val tick: Long) {
    SECOND(SECOND_UNIT, 20L),
    MINUTE(MINUTE_UNIT, SECOND.tick * 60L),
    HOUR(HOUR_UNIT, MINUTE.tick * 60L),
    OTHER("other", 0L);

    fun getUnit(unitName: String): TimerUnit {
        for (unitType in entries) {
            if (unitName == unitType.unit) {
                return unitType
            }
        }
        return OTHER
    }
}