package com.custom.minecraft.message

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class ChatMessage {
    private var message: String = ""
    private var color: NamedTextColor = NamedTextColor.WHITE
    private var players: MutableList<Player> = mutableListOf()

    fun setMessage(message: String): ChatMessage {
        this.message = message
        println("setMessage message = $message")
        return this
    }

    fun setColor(color: NamedTextColor): ChatMessage {
        this.color = color
        println("setColor color = $color")
        return this
    }

    fun setPlayer(player: Player): ChatMessage {
        players.add(player)
        println("setPlayer player = $player")
        return this
    }

    fun setPlayers(players: Collection<Player>): ChatMessage {
        for (player in players) {
            println("setPlayers player = $player")
            if (!players.contains(player)) {
                setPlayer(player)
            }
        }
        return this
    }

    fun show() {
        // メッセージ
        val messageComponent: Component = Component.text(message).color(color)
        if (players.isNotEmpty()) {
            for (player in players) {
                if (player.isOnline) {
                    player.sendMessage(messageComponent)
                }
            }
        }
    }
}