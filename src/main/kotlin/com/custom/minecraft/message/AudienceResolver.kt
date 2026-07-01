package com.custom.minecraft.message

import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * 複数人向け通知で使う Audience の集合を取得するオブジェクト。
 * Messenger から送信対象の判定責務を分離し、呼び出し側が用途に応じて対象を選べるようにする。
 */
object AudienceResolver {

    /**
     * オンライン中の全プレイヤーを Audience として返す。
     *
     * @return 現在オンラインの全プレイヤーを表す Audience 集合。
     */
    fun allOnline(): Iterable<Audience> {
        // 自分用ユーティリティでは呼び出しの簡潔さを優先し、
        // Server 注入ではなく Bukkit から直接取得する
        return Bukkit.getOnlinePlayers()
    }

    /**
     * 指定プレイヤーと同じワールドにいるプレイヤーを Audience として返す。
     *
     * @param player 基準となるプレイヤー。このプレイヤーの所属ワールドを参照する。
     * @return 同じワールドにいるプレイヤーを表す Audience 集合。
     */
    fun sameWorld(player: Player): Iterable<Audience> {
        // Bukkit の World.players をそのまま返し、
        // ワールド単位の判定を Messenger に持ち込まない
        return player.world.players
    }

    /**
     * OP 権限を持つオンラインプレイヤーを Audience として返す。
     *
     * @return OP のオンラインプレイヤーを表す Audience 集合。
     */
    fun operators(): Iterable<Audience> {
        // OP 向け通知はオンラインプレイヤー集合からのフィルタとして扱い、
        // 送信側の API を複雑にしない
        return Bukkit.getOnlinePlayers()
            .filter { it.isOp }
    }

    /**
     * 指定権限を持つオンラインプレイヤーを Audience として返す。
     *
     * @param permission 判定する Bukkit 権限ノード。
     * @return 指定権限を持つオンラインプレイヤーを表す Audience 集合。
     */
    fun permission(permission: String): Iterable<Audience> {
        // 権限文字列は呼び出し元の文脈に依存するため、
        // Resolver は判定だけを行い名前付けや送信内容には関与しない
        return Bukkit.getOnlinePlayers()
            .filter { it.hasPermission(permission) }
    }
}
