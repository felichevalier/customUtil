package com.custom.minecraft.timer

import com.custom.minecraft.message.Messenger
import com.custom.minecraft.timer.domain.FinishReason
import com.custom.minecraft.timer.domain.TimerEvent
import com.custom.minecraft.timer.domain.TimerState
import com.custom.minecraft.timer.listener.TimerListener
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID

/**
 * BossBar で残り時間を共有する秒単位のカウントダウンタイマー。
 *
 * 操作は [start]、[stop]、[reset] の3つに限定し、状態は [TimerState] で管理する。
 */
class MinecraftTimer(
    private val plugin: JavaPlugin,
    private val listener: TimerListener,
) {
    companion object {
        private const val ONE_SECOND_TICKS: Long = 20L
    }

    val timerId: String = UUID.randomUUID().toString()

    var state: TimerState = TimerState.Idle
        private set

    var totalSeconds: Int = 0
        private set

    var remainingSeconds: Int = 0
        private set

    private val targetPlayerIds: MutableSet<UUID> = mutableSetOf()

    private var isTimerMessageEnabled: Boolean = true
    private var startMessage: String = "スタート"
    private var finishMessage: String = "終了"

    private var bossBarColor: BarColor = BarColor.WHITE
    private var bossBarStyle: BarStyle = BarStyle.SOLID
    private var bossBarVisibility: Boolean = true
    private var bossBar: BossBar? = null

    /**
     * 合計時間を設定する。内部では秒へ変換して保持する。
     *
     * @throws IllegalStateException タイマーが実行中の場合。
     * @throws IllegalArgumentException 各値が負数、または秒換算結果が Int の範囲を超える場合。
     */
    fun setDuration(
        hours: Int = 0,
        minutes: Int = 0,
        seconds: Int = 0,
    ): MinecraftTimer {
        checkConfigurable()
        require(hours >= 0) { "hours must be 0 or greater" }
        require(minutes >= 0) { "minutes must be 0 or greater" }
        require(seconds >= 0) { "seconds must be 0 or greater" }

        val durationSeconds = (hours.toLong() * 3600L) + (minutes.toLong() * 60L) + seconds
        require(durationSeconds <= Int.MAX_VALUE) { "duration is too large" }

        totalSeconds = durationSeconds.toInt()
        remainingSeconds = totalSeconds
        return this
    }

    fun bossBarVisibility(isVisible: Boolean): MinecraftTimer {
        checkConfigurable()
        bossBarVisibility = isVisible
        return this
    }

    fun setBossBarColor(color: BarColor): MinecraftTimer {
        checkConfigurable()
        bossBarColor = color
        return this
    }

    fun setBossBarStyle(style: BarStyle): MinecraftTimer {
        checkConfigurable()
        bossBarStyle = style
        return this
    }

    /** 既存の対象をすべて置き換える。 */
    fun setTargets(players: Collection<Player>): MinecraftTimer {
        checkConfigurable()
        targetPlayerIds.clear()
        targetPlayerIds.addAll(players.map(Player::getUniqueId))
        return this
    }

    fun addTarget(player: Player): MinecraftTimer {
        checkConfigurable()
        targetPlayerIds.add(player.uniqueId)
        return this
    }

    fun addTargets(players: Collection<Player>): MinecraftTimer {
        checkConfigurable()
        targetPlayerIds.addAll(players.map(Player::getUniqueId))
        return this
    }

    fun removeTarget(player: Player): MinecraftTimer {
        checkConfigurable()
        targetPlayerIds.remove(player.uniqueId)
        return this
    }

    fun clearTargets(): MinecraftTimer {
        checkConfigurable()
        targetPlayerIds.clear()
        return this
    }

    fun setTitle(start: String = "", finish: String = ""): MinecraftTimer {
        checkConfigurable()
        if (start.isNotEmpty()) {
            startMessage = start
        }
        if (finish.isNotEmpty()) {
            finishMessage = finish
        }
        return this
    }

    fun timerTitleVisibility(isVisible: Boolean): MinecraftTimer {
        checkConfigurable()
        isTimerMessageEnabled = isVisible
        return this
    }

    /**
     * Idle または Finished からタイマーを開始する。
     *
     * @return 開始した場合は true。実行中または時間未設定の場合は false。
     */
    fun start(): Boolean {
        if (state is TimerState.Running || totalSeconds <= 0) {
            return false
        }

        remainingSeconds = totalSeconds
        createBossBar()

        if (isTimerMessageEnabled) {
            showTimerTitle(startMessage)
        }

        val task = object : BukkitRunnable() {
            override fun run() {
                tick()
            }
        }.runTaskTimer(plugin, ONE_SECOND_TICKS, ONE_SECOND_TICKS)

        state = TimerState.Running(task)
        emit(TimerEvent.Started(timerId, totalSeconds))
        return true
    }

    /**
     * 実行中のタイマーを終了する。
     *
     * @param reason 終了理由。通常停止では [FinishReason.STOPPED] を使用する。
     * @return 停止した場合は true。実行中でなければ false。
     */
    fun stop(reason: FinishReason = FinishReason.STOPPED): Boolean {
        val running = state as? TimerState.Running ?: return false

        if (!running.task.isCancelled) {
            running.task.cancel()
        }
        removeBossBar()
        state = TimerState.Finished
        emit(TimerEvent.Finished(timerId, reason))
        return true
    }

    /**
     * タイマーを初期状態へ戻す。終了イベントは通知しない。
     * 設定、対象プレイヤー、Listener は維持される。
     */
    fun reset() {
        val running = state as? TimerState.Running
        if (running != null && !running.task.isCancelled) {
            running.task.cancel()
        }

        removeBossBar()
        remainingSeconds = totalSeconds
        state = TimerState.Idle
    }

    private fun tick() {
        if (state !is TimerState.Running) {
            return
        }

        remainingSeconds = (remainingSeconds - 1).coerceAtLeast(0)
        updateBossBar()
        emit(TimerEvent.Tick(timerId, remainingSeconds))

        if (remainingSeconds == 0) {
            if (isTimerMessageEnabled) {
                showTimerTitle(finishMessage)
            }
            stop(FinishReason.COMPLETED)
        }
    }

    private fun checkConfigurable() {
        check(state !is TimerState.Running) {
            "Timer configuration cannot be changed while running"
        }
    }

    private fun createBossBar() {
        removeBossBar()

        bossBar = Bukkit.createBossBar(getBossBarTitle(), bossBarColor, bossBarStyle).also { bar ->
            onlineTargets().forEach(bar::addPlayer)
            bar.progress = 1.0
            bar.isVisible = bossBarVisibility
        }
    }

    private fun updateBossBar() {
        val progress = if (totalSeconds == 0) {
            0.0
        } else {
            remainingSeconds.toDouble() / totalSeconds.toDouble()
        }

        bossBar?.apply {
            setTitle(getBossBarTitle())
            this.progress = progress.coerceIn(0.0, 1.0)
        }
    }

    private fun removeBossBar() {
        bossBar?.removeAll()
        bossBar = null
    }

    private fun onlineTargets(): List<Player> {
        return targetPlayerIds
            .mapNotNull(Bukkit::getPlayer)
            .filter(Player::isOnline)
    }

    private fun getBossBarTitle(): String = "残り時間: ${formatAsHms(remainingSeconds)}"

    private fun formatAsHms(totalSeconds: Int): String {
        val safeSeconds = totalSeconds.coerceAtLeast(0)
        val hours = safeSeconds / 3600
        val minutes = (safeSeconds % 3600) / 60
        val seconds = safeSeconds % 60
        return "${hours}h${minutes}m${seconds}s"
    }

    private fun showTimerTitle(message: String) {
        Messenger.showTitle(
            audiences = onlineTargets(),
            title = Component.text(message, NamedTextColor.WHITE),
        )
    }

    private fun emit(event: TimerEvent) {
        listener.onEvent(event)
    }
}
