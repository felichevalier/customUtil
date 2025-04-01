package com.custom.minecraft.message

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class ChatMessage {
    private var message: String = ""
    private var color: NamedTextColor = NamedTextColor.WHITE
    private var players: MutableList<Player> = mutableListOf()

    fun setMessage(message: String) {
        this.message = message
    }

    fun setColor(color: NamedTextColor) {
        this.color = color
    }

    fun setPlayer(player: Player) {
        players.add(player)
    }

    fun setPlayers(players: List<Player>) {
        for (player in players) {
            if (!players.contains(player)) {
                setPlayer(player)
            }
        }
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