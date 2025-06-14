package com.custom.minecraft.message

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

class ChatMessage {
    private var message: String = ""
    private var color: NamedTextColor = NamedTextColor.WHITE
    private var players: MutableList<Player> = mutableListOf()

    fun setMessage(message: String): ChatMessage {
        this.message = message
        println("ChatMessage setMessage message = $message")
        return this
    }

    fun setColor(color: NamedTextColor): ChatMessage {
        this.color = color
        println("ChatMessage setColor color = $color")
        return this
    }

    fun setPlayer(player: Player): ChatMessage {
        players.add(player)
        println("ChatMessage setPlayer player = $player")
        return this
    }

    fun setPlayers(targetPlayers: Collection<Player>): ChatMessage {
        for (player in targetPlayers) {
            println("ChatMessage setPlayers player = $player")
            if (!players.contains(player)) {
                setPlayer(player)
            }
        }
        return this
    }

    fun show() {
        // メッセージ
        val messageComponent: Component = Component.text(message).color(color)
        println("ChatMessage show players = $players, messageComponent = $messageComponent")
        if (players.isNotEmpty()) {
            for (player in players) {
                println("player = $player")
                if (player.isOnline) {
                    player.sendMessage(messageComponent)
                }
            }
        }
    }
}