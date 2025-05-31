package com.custom.minecraft.timer

import com.custom.minecraft.timer.TimerUnit.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.time.Duration

class MinecraftTimer(private val plugin: JavaPlugin,private val listener: TimerListener) {

    private lateinit var timerEntity: TimerEntity
    private var bossBarColor: BarColor = BarColor.WHITE
    private var bossBarStyle: BarStyle = BarStyle.SOLID
    private var isRunning: Boolean = false
    private var isPaused: Boolean = false
    private lateinit var bossBar: BossBar

    fun setTimer(timerEntity: TimerEntity, showBossBar: Boolean = true): MinecraftTimer {
        if (isRunning || ::bossBar.isInitialized) {
            bossBar.removeAll()
        }
        val title = when (timerEntity.timerUnit) {
            SECOND -> "残り時間: ${timerEntity.time}秒"
            MINUTE -> "残り時間: ${timerEntity.time}分"
            HOUR -> "残り時間: ${timerEntity.time}時間"
            OTHER -> ""
        }
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
                setTitle(timerEntity.startMessage)
            }
            when (timerEntity.timerUnit) {
                SECOND -> {
                    val title = "残り時間: ${timerEntity.time}秒"
                    countTimer(SECOND.tick, title)
                    println(title)
                }
                MINUTE -> {
                    val title = "残り時間:${timerEntity.time}分"
                    countTimer(MINUTE.tick, title)
                    println(title)
                }
                HOUR -> {
                    val title = "残り時間:${timerEntity.time}時間"
                    countTimer(MINUTE.tick, title)
                    println(title)
                }
                OTHER -> {
                    println("設定時間の単位が不正です")
                    result = false
                }
            }
        }

        listener.onStartTimer(result)
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

    fun finishTimer() {
        isRunning = false
        isPaused = false
        bossBar.removeAll()
        listener.onFinishTimer()
    }

    private fun countTimer(tick: Long, title: String) {
        object: BukkitRunnable() {
            override fun run() {
                if (!isPaused) {
                    timerEntity.time -= 1
                    bossBar.setTitle(title)
                    if (timerEntity.time <= 0) {
                        if (timerEntity.isTimerMessageEnabled) {
                            setTitle(timerEntity.finishMessage)
                        }
                        finishTimer()
                        cancel()
                    }
                    val progress: Double = timerEntity.time.toDouble() / timerEntity.time
                    bossBar.progress = progress
                    updateTimer()
                }
            }
        }.runTaskTimer(plugin, tick, tick)
    }

    private fun setTitle(message: String) {
        for (player: Player in timerEntity.targetPlayers) {
            // タイトルのテキストと色を設定
            val titleComponent: Component = Component.text(message).color(NamedTextColor.WHITE)

            // サブタイトル
            val subTitleComponent: Component = Component.text("")

            // 表示時間を設定
            val times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(1), Duration.ofMillis(500))
            val title = Title.title(titleComponent, subTitleComponent, times)
            player.showTitle(title)
        }
    }
}