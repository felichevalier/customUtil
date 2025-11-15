package com.custom.minecraft.item.returnstick

import cloud.commandframework.Command
import cloud.commandframework.execution.CommandExecutionCoordinator
import cloud.commandframework.paper.PaperCommandManager
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.java.JavaPlugin
import java.util.function.Function

class ReturnStick {

    companion object {
        const val STICK_NAME: String = "戻り棒"
        private const val GIVE_STICK_COMMAND: String = "giveStick"
        val DEATH_PLAYER_LIST: MutableList<Player> = mutableListOf()
        lateinit var javaPlugin: JavaPlugin
        lateinit var instance: ReturnStick
    }


    fun init(plugin: JavaPlugin): ReturnStick {
        javaPlugin = plugin
        instance = this
        javaPlugin.server.pluginManager.registerEvents(ReturnStickEventListener(), javaPlugin)
        return this
    }

    fun isReturnStick(item: ItemStack): Boolean {
        return (item.type == Material.CARROT_ON_A_STICK
                && item.itemMeta.displayName() == Component.text(STICK_NAME))
    }

    fun giveStick(player: Player) {
        // 棒
        val stick = ItemStack(Material.CARROT_ON_A_STICK)
        // 棒のメタ情報
        val meta: ItemMeta = stick.itemMeta
        // 表示名を変更
        meta.displayName(Component.text(STICK_NAME))
        stick.itemMeta = meta
        // プレイヤーのインベントリに戻り棒を追加
        val inventory: Inventory = player.inventory
        inventory.addItem(stick)
    }

    fun returnSpawnLocation(player: Player) {
        val world: World = player.server.worlds
            .find { world ->
                javaPlugin.logger.info("world.environment = ${world.environment}")
                world.environment == World.Environment.NORMAL
            } ?: return
        player.teleport(world.spawnLocation)
    }

    fun giveStick(players: Collection<Player>) {
        // インベントリに追加
        for (player: Player in players) {
            giveStick(player)
        }
    }

    fun enableCommand() {
        val manager: PaperCommandManager<CommandSender> = PaperCommandManager(
            javaPlugin,
            CommandExecutionCoordinator.simpleCoordinator(),
            Function.identity(),
            Function.identity()
        )

        val builder: Command.Builder<CommandSender> = manager.commandBuilder(GIVE_STICK_COMMAND)

        builder.handler { ctx ->
            val sender: CommandSender = ctx.sender
            if (sender is Player) {
                giveStick(sender)
            }
        }
    }
}