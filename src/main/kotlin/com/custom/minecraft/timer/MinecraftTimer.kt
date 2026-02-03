package com.custom.minecraft.timer
import com.custom.minecraft.message.TitleMessage
import com.custom.minecraft.timer.domain.TimerEvent
import com.custom.minecraft.timer.listener.TimerListener
import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

/**
 * ボスバーで残り時間を共有するカウントダウンタイマー。
 * TimerListener へ状態を通知しつつ、指定プレイヤーへタイトルや進捗を表示する。
 */
class MinecraftTimer(private val plugin: JavaPlugin, private val listener: TimerListener) {
    companion object {
        // 1 秒を表す tick 数
        private const val ONE_SECOND_TICKS: Long = 20L
    }

    // 設定された合計秒数と現在残り秒数
    private var totalSeconds: Int = 0
    private var remainingSeconds: Int = 0
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

    // タイマーリスナーを保持
    private val listeners = mutableListOf<TimerListener>()

    // タイマーを設定（h/m/s を直接指定）
    fun setTimer(hours: Int = 0, minutes: Int = 0, seconds: Int = 0): MinecraftTimer {
        this.totalSeconds = (hours * 3600) + (minutes * 60) + seconds
        this.remainingSeconds = totalSeconds
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

    fun addListener(listener: TimerListener): MinecraftTimer {
        listeners.add(listener)
        return this
    }

    fun removeListener(listener: TimerListener) {
        listeners.remove(listener)
    }

    private fun emit(event: TimerEvent) {
        listeners.forEach { it.onEvent(event) }
    }

    /**
     * タイマーを開始する。すでに実行中なら一度終了してから再度開始する。
     */
    fun startTimer() {
        var result = false

        if (isRunning) {
            finishTimer()
        }
        isRunning = true
        remainingSeconds = totalSeconds
        if (totalSeconds <= 0) {
            emit(TimerEvent.Start(false))
            return
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
        countDownTimer(ONE_SECOND_TICKS)
        plugin.logger.info(title)
        emit(TimerEvent.Start(true, remainingSeconds))
    }

    /**
     * 外部から強制的にタイマーを終了させる。
     */
    fun stopTimer() {
        if (::timerTask.isInitialized) {
            timerTask.cancel()
        }
        finishTimer()
    }

    /**
     * 一時停止中のタイマーを再開する。
     */
    fun resumeTimer() {
        isPaused = false
        emit(TimerEvent.Resume)
    }

    fun updateTimer() {
        emit(TimerEvent.Tick(remainingSeconds))
    }

    /**
     * タイマーを一時停止する。
     */
    fun pauseTimer() {
        isPaused = true
        emit(TimerEvent.Pause)
    }

    /**
     * ボスバーのクリーンアップとリスナー通知をまとめて行う。
     */
    private fun finishTimer() {
        isRunning = false
        isPaused = false
        if (::bossBar.isInitialized) {
            bossBar.removeAll()
        }
        if (::timerTask.isInitialized && !timerTask.isCancelled) {
            timerTask.cancel()
        }
        emit(TimerEvent.Finish)
        plugin.logger.info("finishTimer")
    }

    /**
     * 指定 tick 間隔で残り時間を減算し、進捗更新や終了処理を行う。
     */
    private fun countDownTimer(tick: Long) {
        if (totalSeconds <= 0) {
            finishTimer()
            return
        }
        timerTask = object: BukkitRunnable() {
            override fun run() {
                if (!isPaused) {
                    remainingSeconds -= 1
                    bossBar.setTitle(getBossBarTitle())
                    plugin.logger.info("time = $remainingSeconds")
                    if (remainingSeconds <= 0) {
                        if (isTimerMessageEnabled) {
                            val titleMessage = TitleMessage()
                            titleMessage.setTitle(finishMessage).setPlayers(targetPlayers).show()
                        }
                        finishTimer()
                        return
                    }
                    val progress: Double = remainingSeconds.toDouble() / totalSeconds.toDouble()
                    bossBar.progress = progress
                    plugin.logger.info("progress = $progress")
                    updateTimer()
                }
            }
        }.runTaskTimer(plugin, tick, tick)
    }

    private fun getBossBarTitle(): String {
        val timeText = formatAsHms(remainingSeconds)
        return "残り時間: $timeText"
    }

    // 常に h/m/s を含む表示用文字列へ変換する（例: 0h0m20s）
    private fun formatAsHms(totalSeconds: Int): String {
        val safeSeconds = totalSeconds.coerceAtLeast(0)
        val hours = safeSeconds / 3600
        val minutes = (safeSeconds % 3600) / 60
        val seconds = safeSeconds % 60
        return "${hours}h${minutes}m${seconds}s"
    }
}
