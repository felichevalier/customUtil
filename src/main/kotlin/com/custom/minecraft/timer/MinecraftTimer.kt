package com.custom.minecraft.timer
import com.custom.minecraft.message.TitleMessage
import com.custom.minecraft.timer.TimerUnit.*
import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

class MinecraftTimer(private val plugin: JavaPlugin, private val listener: TimerListener) {

    // タイマーの単位
    private var timerUnit: TimerUnit = SECOND
    // 時間
    private var time: Int = 0
    // タイマー表示の対象プレイヤー
    private val targetPlayers: MutableList<Player> = mutableListOf()
    // 開始/終了時のメッセージを表示するのか
    private var isTimerMessageEnabled: Boolean = true
    // 開始/終了時のメッセージ
    private var startMessage: String = "スタート"
    private var finishMessage: String = "終了"
    // ボスバーの設定
    private var bossBarColor: BarColor = BarColor.WHITE
    private var bossBarStyle: BarStyle = BarStyle.SOLID
    private var bossBarVisibility: Boolean = true
    // タイマー実行中フラグ
    private var isRunning: Boolean = false
    // タイマー一時停止フラグ
    private var isPaused: Boolean = false

    // ボスバー
    private lateinit var bossBar: BossBar
    // タイトル
    private lateinit var title: String

    // runnable
    private lateinit var timerTask: BukkitTask

    // タイマーを設定
    fun setTimer(time: Int, timerUnit: TimerUnit = SECOND): MinecraftTimer {
        this.timerUnit = timerUnit
        this.time = time

        return this
    }

    fun bossBarVisibility(isVisible: Boolean): MinecraftTimer {
        this.bossBarVisibility = isVisible
        return this
    }

    fun setBossBarColor(color: BarColor): MinecraftTimer {
        this.bossBarColor = color
        return this
    }

    fun setBossBarStyle(style: BarStyle): MinecraftTimer {
        this.bossBarStyle = style
        return this
    }

    fun setTargetPlayer(player: Player): MinecraftTimer {
        this.targetPlayers.add(player)
        return this
    }

    fun setTargetPlayers(players: Collection<Player>): MinecraftTimer {
        this.targetPlayers.addAll(players)
        return this
    }

    fun setTitle(start: String = "", finish: String = ""): MinecraftTimer {
        if (start.isNotEmpty()) {
            this.startMessage = start
        }
        if (finish.isNotEmpty()) {
            this.finishMessage = finish
        }
        return this
    }

    fun timerVisibility(isVisible: Boolean): MinecraftTimer {
        this.isTimerMessageEnabled = isVisible
        return this
    }

    fun startTimer() {
        var result = false

        if (::bossBar.isInitialized) {
            if (isRunning) {
                finishTimer()
            }
            // ボスバーの上の表示
            title = getBossBarTitle()
            // ボスバーの設定
            bossBar = Bukkit.createBossBar(title, bossBarColor, bossBarStyle)
            for (player in targetPlayers) {
                bossBar.addPlayer(player)
            }
            bossBar.progress = 1.0
            bossBar.isVisible = bossBarVisibility

            if (isTimerMessageEnabled) {
                val title = TitleMessage()
                title.setTitle(startMessage).setPlayers(targetPlayers).show()
            }
            when (timerUnit) {
                SECOND -> {
                    countDownTimer(SECOND.tick)
                    plugin.logger.info(title)
                }
                MINUTE -> {
                    countDownTimer(MINUTE.tick)
                    plugin.logger.info(title)
                }
                HOUR -> {
                    countDownTimer(MINUTE.tick)
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
        timerTask.cancel()
        finishTimer()
    }

    fun resumeTimer() {
        isPaused = false
        listener.onResumeTimer()
    }

    fun updateTimer() {
        listener.onUpdateTimer(time)
    }

    fun pauseTimer() {
        isPaused = true
        listener.onPauseTimer()
    }

    private fun finishTimer() {
        isRunning = false
        isPaused = false
        bossBar.removeAll()
        timerTask.cancel()
        listener.onFinishTimer()
        plugin.logger.info("finishTimer")
    }

    private fun countDownTimer(tick: Long) {
        val totalTime: Int = time
        timerTask = object: BukkitRunnable() {
            override fun run() {
                if (!isPaused) {
                    time -= 1
                    bossBar.setTitle(getBossBarTitle())
                    plugin.logger.info("time = $time")
                    if (time <= 0) {
                        if (isTimerMessageEnabled) {
                            val titleMessage = TitleMessage()
                            titleMessage.setTitle(finishMessage).setPlayers(targetPlayers).show()
                        }
                        finishTimer()
                    }
                    val progress: Double = time.toDouble() / totalTime
                    bossBar.progress = progress
                    plugin.logger.info("progress = $progress")
                    updateTimer()
                }
            }
        }.runTaskTimer(plugin, tick, tick)
    }

    private fun getBossBarTitle(): String {
        // ボスバーの上の表示
        return when (timerUnit) {
            SECOND -> "残り時間: ${time}秒"
            MINUTE -> "残り時間: ${time}分"
            HOUR -> "残り時間: ${time}時間"
            OTHER -> ""
        }
    }
}