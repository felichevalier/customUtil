package com.custom.minecraft.item.returnstick

import com.custom.minecraft.item.returnstick.ReturnStick.Companion.instance
import com.custom.minecraft.item.returnstick.ReturnStick.Companion.DEATH_PLAYER_LIST
import com.custom.minecraft.item.returnstick.ReturnStick.Companion.javaPlugin
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack

class ReturnStickEventListener: Listener {

    @EventHandler
    fun onItemInteract(event: PlayerInteractEvent) {
        val player: Player = event.player
        val item: ItemStack = event.item ?: return
        println("player = $player, item.type = ${item.type}, item.displayName = ${item.displayName()}")
        if (instance.isReturnStick(item)) {
            instance.returnSpawnLocation(player)
        }
    }

    @EventHandler
    fun onDeathEvent(event: PlayerDeathEvent) {
        val player = event.player
        val drops = event.drops

        if (drops.isEmpty()) {
            javaPlugin.logger.warning("drops is Empty")
            return
        }

        val iterator = drops.iterator()
        while (iterator.hasNext()) {
            val drop = iterator.next()
            if (instance.isReturnStick(drop)) {
                DEATH_PLAYER_LIST.add(player)
                iterator.remove()
            }
        }
    }

    @EventHandler
    fun onRespawnEvent(event: PlayerRespawnEvent) {
        val player = event.player
        if (DEATH_PLAYER_LIST.contains(player)) {
            instance.giveStick(player)
            DEATH_PLAYER_LIST.remove(player)
        }
    }
}