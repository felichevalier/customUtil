package com.custom.minecraft.test

import com.custom.minecraft.item.returnstick.ReturnStick
import com.custom.minecraft.timer.MinecraftTimer
import com.custom.minecraft.timer.domain.FinishReason
import com.custom.minecraft.timer.domain.TimerEvent
import com.custom.minecraft.timer.listener.TimerListener
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin


class Test: JavaPlugin(), TimerListener {

    private lateinit var timer: MinecraftTimer

    override fun onEnable() {
        timer = MinecraftTimer(this, this)

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val registrar: Commands = event.registrar()

            val root: LiteralCommandNode<CommandSourceStack> = Commands.literal("testTimer")
                .then(Commands.literal("set")
                    .then(Commands.argument("hour", IntegerArgumentType.integer())
                        .then(Commands.argument("minute", IntegerArgumentType.integer())
                            .then(Commands.argument("second", IntegerArgumentType.integer())
                                .executes { ctx ->
                                    val hour: Int = ctx.getArgument("hour", Int::class.java)
                                    val minute: Int = ctx.getArgument("minute", Int::class.java)
                                    val second: Int = ctx.getArgument("second", Int::class.java)
                                    timer.reset()
                                    timer
                                        .setDuration(hour, minute, second)
                                        .setTargets(server.onlinePlayers)
                                    return@executes if (timer.start()) 1 else 0
                                }
                            )

                        )
                    )
                )
                .then(Commands.literal("stop")
                    .executes { _ ->
                        return@executes if (timer.stop()) 1 else 0
                    })
                .then(Commands.literal("reset")
                    .executes { _ ->
                        timer.reset()
                        return@executes 1
                    })
                .build()

            registrar.register(root)

            val testRoot: LiteralCommandNode<CommandSourceStack> = Commands.literal("test").executes { ctx ->
                val returnStick = ReturnStick()
                returnStick.init(this)

                val player: Player = ctx.source.executor as? Player ?: run {
                    logger.info("プレイヤー以外が実行しました")
                    return@executes 0
                }
                returnStick.giveStick(player)
                return@executes 1
            }.build()

            registrar.register(testRoot)
        }

    }

    override fun onDisable() {
        timer.stop(FinishReason.CANCELLED)
    }

    override fun onEvent(event: TimerEvent) {
        when (event) {
            is TimerEvent.Started -> {
                logger.info("timer=${event.timerId}, totalSeconds=${event.totalSeconds}")
            }
            is TimerEvent.Tick -> {
                logger.info("timer=${event.timerId}, remainingSeconds=${event.remainingSeconds}")
            }
            is TimerEvent.Finished -> {
                logger.info("timer=${event.timerId}, reason=${event.reason}")
            }
        }
    }
}
