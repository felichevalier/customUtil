package com.custom.minecraft.item.returnstick

import com.custom.minecraft.item.returnstick.ReturnStick.Companion.STICK_NAME
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class ReturnStickEventListener: Listener {

    @EventHandler
    fun onItemInteract(event: PlayerInteractEvent) {
        val player: Player = event.player
        val item: ItemStack = event.item ?: return
        println("player = $player, item.type = ${item.type}, item.displayName = ${item.displayName()}")
        if (item.type == Material.CARROT_ON_A_STICK && item.displayName() == Component.text(STICK_NAME)) {
            val world: World = player.server.worlds.find { world ->
                println("world.environment = ${world.environment}")
                world.environment == World.Environment.NORMAL
            } ?: return

            player.teleport(world.spawnLocation)
            println("teleported")
        }
    }
}