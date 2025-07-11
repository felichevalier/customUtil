package com.custom.minecraft.item.returnstick

import com.custom.minecraft.item.returnstick.ReturnStick.Companion.STICK_NAME
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class ReturnStickEventListener: Listener {

    @EventHandler
    fun onItemInteract(event: PlayerInteractEvent) {
        val player: Player = event.player
        val item: Material = event.item?.type ?: return
        if (item == Material.CARROT_ON_A_STICK && item.name == STICK_NAME) {
            val world: World = player.server.worlds.find { world ->
                world.environment == World.Environment.NORMAL
            } ?: return

            player.teleport(world.spawnLocation)
        }
    }
}