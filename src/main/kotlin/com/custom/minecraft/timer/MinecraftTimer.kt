package com.custom.minecraft.timer

import com.custom.minecraft.message.TitleMessage
import com.custom.minecraft.timer.TimerUnit.*
import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

class MinecraftTimer(private val plugin: JavaPlugin, private val listener: TimerListener) {

    private lateinit var timerEntity: TimerEntity
    private var bossBarColor: BarColor = BarColor.WHITE
    private var bossBarStyle: BarStyle = BarStyle.SOLID
    private var isRunning: Boolean = false
    private var isPaused: Boolean = false
    private var cancelTimer: Boolean = false
    private lateinit var bossBar: BossBar

    // タイマーを設定
    fun setTimer(timerEntity: TimerEntity, showBossBar: Boolean = true): MinecraftTimer {
        if (isRunning || ::bossBar.isInitialized) {
            bossBar.removeAll()
        }
        // ボスバーの上の表示
        val title = when (timerEntity.timerUnit) {
            SECOND -> "残り時間: ${timerEntity.time}秒"
            MINUTE -> "残り時間: ${timerEntity.time}分"
            HOUR -> "残り時間: ${timerEntity.time}時間"
            OTHER -> ""
        }
        // ボスバーの設定
        bossBar = Bukkit.createBossBar(title, bossBarColor, bossBarStyle)
        for (player in timerEntity.targetPlayers) {
            bossBar.addPlayer(player)
        }
        bossBar.progress = 1.0
        bossBar.isVisible = showBossBar
        this.timerEntity = timerEntity
        return this
    }

    fun setTimerBarOption(color: BarColor = BarColor.WHITE, style: BarStyle = BarStyle.SOLID): MinecraftTimer {
        bossBarColor = color
        bossBarStyle = style
        return this
    }

    fun startTimer() {
        var result = true
        if (isRunning) {
            result = false
        } else {
            if (timerEntity.isTimerMessageEnabled) {
                val title = TitleMessage()
                title.setTitle(timerEntity.startMessage).setPlayers(timerEntity.targetPlayers).show()
            }
            when (timerEntity.timerUnit) {
                SECOND -> {
                    val title = "残り時間: ${timerEntity.time}秒"
                    countDownTimer(SECOND.tick, title)
                    plugin.logger.info(title)
                }
                MINUTE -> {
                    val title = "残り時間:${timerEntity.time}分"
                    countDownTimer(MINUTE.tick, title)
                    plugin.logger.info(title)
                }
                HOUR -> {
                    val title = "残り時間:${timerEntity.time}時間"
                    countDownTimer(MINUTE.tick, title)
                    plugin.logger.info(title)
                }
                OTHER -> {
                    plugin.logger.info("設定時間の単位が不正です")
                    result = false
                }
            }
        }

        listener.onStartTimer(result)
    }

    fun stopTimer() {
        cancelTimer = true
    }

    fun resumeTimer() {
        isPaused = false
        listener.onResumeTimer()
    }

    fun updateTimer() {
        listener.onUpdateTimer(timerEntity.time)
    }

    fun pauseTimer() {
        isPaused = true
        listener.onPauseTimer()
    }

    private fun finishTimer() {
        isRunning = false
        isPaused = false
        bossBar.removeAll()
        listener.onFinishTimer()
    }

    private fun countDownTimer(tick: Long, title: String) {
        val totalTime: Int = timerEntity.time
        object: BukkitRunnable() {
            override fun run() {
                if (cancelTimer) {
                    finishTimer()
                    cancel()
                } else if (!isPaused) {
                    timerEntity.time -= 1
                    bossBar.setTitle(title)
                    if (timerEntity.time <= 0) {
                        if (timerEntity.isTimerMessageEnabled) {
                            val titleMessage = TitleMessage()
                            titleMessage.setTitle(timerEntity.finishMessage).setPlayers(timerEntity.targetPlayers).show()
                        }
                        finishTimer()
                        cancel()
                    }
                    val progress: Double = timerEntity.time.toDouble() / totalTime
                    bossBar.progress = progress
                    plugin.logger.info("progress = $progress")
                    updateTimer()
                }
            }
        }.runTaskTimer(plugin, tick, tick)
    }
}