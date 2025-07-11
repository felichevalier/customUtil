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
    }
    private val giveStickCommand: String = "giveStick"
    private lateinit var javaPlugin: JavaPlugin

    fun init(plugin: JavaPlugin): ReturnStick {
        javaPlugin = plugin
        javaPlugin.server.pluginManager.registerEvents(ReturnStickEventListener(), javaPlugin)
        return this
    }

    fun giveStick(players: Collection<Player>) {
        // インベントリに追加
        for (player: Player in players) {
            giveStick(player)
        }
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
        val world: World = javaPlugin.server.worlds
            .find { it.environment == World.Environment.NORMAL } ?: return
        player.teleport(world.spawnLocation)
    }

    fun enableCommand() {
        val manager: PaperCommandManager<CommandSender> = PaperCommandManager(
            javaPlugin,
            CommandExecutionCoordinator.simpleCoordinator(),
            Function.identity(),
            Function.identity()
        )

        val builder: Command.Builder<CommandSender> = manager.commandBuilder(giveStickCommand)

        builder.handler { ctx ->
            val sender: CommandSender = ctx.sender
            if (sender is Player) {
                giveStick(sender)
            }
        }
    }

}