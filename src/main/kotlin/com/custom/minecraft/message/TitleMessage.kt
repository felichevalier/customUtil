package com.custom.minecraft.message

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player
import java.time.Duration

class TitleMessage {

    private var title: String = ""
    private var subTitle: String = ""
    private var color: NamedTextColor = NamedTextColor.WHITE
    private var fadeIn: Duration = Duration.ofMillis(500)
    private var stay: Duration = Duration.ofSeconds(1)
    private var fadeOut: Duration = Duration.ofMillis(500)
    private var players: MutableList<Player> = mutableListOf()

    fun setTitle(title: String): TitleMessage {
        this.title = title
        println("TitleMessage setTitle title = $title")
        return this
    }

    fun setSubTitle(subTitle: String): TitleMessage {
        this.subTitle = subTitle
        println("TitleMessage setSubTitle subTitle = $subTitle")
        return this
    }

    fun setColor(color: NamedTextColor): TitleMessage {
        this.color = color
        println("TitleMessage setColor color = $color")
        return this
    }

    fun setFadeIn(fadeIn: Duration): TitleMessage {
        this.fadeIn = fadeIn
        println("TitleMessage setFadeIn fadeIn = $fadeIn")
        return this
    }

    fun setStay(stay: Duration): TitleMessage {
        this.stay = stay
        println("TitleMessage setStay stay = $stay")
        return this
    }

    fun setFadeOut(fadeOut: Duration): TitleMessage {
        this.fadeOut = fadeOut
        println("TitleMessage setFadeOut fadeOut = $fadeOut")
        return this
    }

    fun setPlayer(player: Player): TitleMessage {
        players.add(player)
        println("TitleMessage setPlayer player = $player、players = $players")
        return this
    }

    fun setPlayers(players: Collection<Player>): TitleMessage {
        for (player in players) {
            println("TitleMessage setPlayers player = $player")
            if (!this.players.contains(player)) {
                setPlayer(player)
            }
        }
        return this
    }

    // タイトルを表示
    fun show() {
        // タイトルのテキストと色を設定
        val titleComponent: Component = Component.text(title).color(color)
        // サブタイトル
        val subTitleComponent: Component = Component.text(subTitle).color(color)
        // 表示時間を設定
        val times = Title.Times.times(fadeIn, stay, fadeOut)
        val title = Title.title(titleComponent, subTitleComponent, times)
        if (players.isNotEmpty()) {
            for (player in players) {
                println("TitleMessage showMessage player = $player")
                if (player.isOnline) {
                    player.showTitle(title)
                    println("TitleMessage showTitle title = $title")
                } else {
                    println("TitleMessage $player is offline")
                }
            }
        } else {
            println("TitleMessage players is empty")
        }
    }
}