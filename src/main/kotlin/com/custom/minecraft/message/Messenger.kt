package com.custom.minecraft.message

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.title.Title
import java.time.Duration

/**
 * Audience に対してチャットメッセージやタイトルを送信する通知ユーティリティ。
 * 送信対象の解決は行わず、呼び出し元または AudienceResolver が決めた対象へ表示する責務を持つ。
 */
object Messenger {

    private val miniMessage = MiniMessage.miniMessage()

    /**
     * 通常テキストを単一 Audience に送信する。
     *
     * @param audience 送信先。Player や Console など Adventure の Audience を想定する。
     * @param message 送信する通常テキスト。MiniMessage としては解釈しない。
     * @param color メッセージ全体に適用する文字色。
     */
    fun sendText(
        audience: Audience,
        message: String,
        color: NamedTextColor = NamedTextColor.WHITE,
    ) {
        // 単色テキスト用途では MiniMessage を通さず、
        // 装飾タグの誤解釈を避ける
        audience.sendMessage(Component.text(message, color))
    }

    /**
     * 通常テキストを複数 Audience に送信する。
     *
     * @param audiences 送信先の集合。空の場合は何も送信しない。
     * @param message 送信する通常テキスト。MiniMessage としては解釈しない。
     * @param color メッセージ全体に適用する文字色。
     */
    fun sendText(
        audiences: Iterable<Audience>,
        message: String,
        color: NamedTextColor = NamedTextColor.WHITE,
    ) {
        // 同じ Component を使い回して、複数送信時の生成コストと表記ゆれを抑える
        val component = Component.text(message, color)
        audiences.forEach { it.sendMessage(component) }
    }

    /**
     * MiniMessage 文字列を単一 Audience に送信する。
     *
     * @param audience 送信先。Player や Console など Adventure の Audience を想定する。
     * @param message MiniMessage として解釈する文字列。
     */
    fun sendMini(
        audience: Audience,
        message: String,
    ) {
        // MiniMessage 用 API であることを明確に分け、
        // 通常テキスト送信とタグ解釈の責務を混ぜない
        audience.sendMessage(miniMessage.deserialize(message))
    }

    /**
     * MiniMessage 文字列を複数 Audience に送信する。
     *
     * @param audiences 送信先の集合。空の場合は何も送信しない。
     * @param message MiniMessage として解釈する文字列。
     */
    fun sendMini(
        audiences: Iterable<Audience>,
        message: String,
    ) {
        // 同じ MiniMessage 文字列は一度だけ Component 化し、
        // 全送信先へ同一内容を届ける
        val component = miniMessage.deserialize(message)
        audiences.forEach { it.sendMessage(component) }
    }

    /**
     * Component で構築したタイトルを単一 Audience に表示する。
     *
     * @param audience 表示先。タイトル表示に対応した Audience を想定する。
     * @param title メインタイトルとして表示する Component。
     * @param subtitle サブタイトルとして表示する Component。不要な場合は空 Component。
     * @param fadeIn 表示開始時のフェード時間。
     * @param stay 表示を維持する時間。
     * @param fadeOut 表示終了時のフェード時間。
     */
    fun showTitle(
        audience: Audience,
        title: Component,
        subtitle: Component = Component.empty(),
        fadeIn: Duration = Duration.ofMillis(500),
        stay: Duration = Duration.ofSeconds(1),
        fadeOut: Duration = Duration.ofMillis(500),
    ) {
        // タイトル時間のデフォルトをここに集約し、
        // 呼び出し側で毎回 Times を組み立てる必要をなくす
        audience.showTitle(
            Title.title(
                title,
                subtitle,
                Title.Times.times(fadeIn, stay, fadeOut),
            ),
        )
    }

    /**
     * Component で構築したタイトルを複数 Audience に表示する。
     *
     * @param audiences 表示先の集合。空の場合は何も表示しない。
     * @param title メインタイトルとして表示する Component。
     * @param subtitle サブタイトルとして表示する Component。不要な場合は空 Component。
     * @param fadeIn 表示開始時のフェード時間。
     * @param stay 表示を維持する時間。
     * @param fadeOut 表示終了時のフェード時間。
     */
    fun showTitle(
        audiences: Iterable<Audience>,
        title: Component,
        subtitle: Component = Component.empty(),
        fadeIn: Duration = Duration.ofMillis(500),
        stay: Duration = Duration.ofSeconds(1),
        fadeOut: Duration = Duration.ofMillis(500),
    ) {
        // 複数人に同じタイトルを出すため、
        // Title インスタンスを一度だけ生成して共有する
        val adventureTitle = Title.title(
            title,
            subtitle,
            Title.Times.times(fadeIn, stay, fadeOut),
        )

        audiences.forEach { it.showTitle(adventureTitle) }
    }

    /**
     * MiniMessage 文字列からタイトルを作り、単一 Audience に表示する。
     *
     * @param audience 表示先。タイトル表示に対応した Audience を想定する。
     * @param title MiniMessage として解釈するメインタイトル文字列。
     * @param subtitle MiniMessage として解釈するサブタイトル文字列。空白のみなら表示しない。
     * @param fadeIn 表示開始時のフェード時間。
     * @param stay 表示を維持する時間。
     * @param fadeOut 表示終了時のフェード時間。
     */
    fun showMiniTitle(
        audience: Audience,
        title: String,
        subtitle: String = "",
        fadeIn: Duration = Duration.ofMillis(500),
        stay: Duration = Duration.ofSeconds(1),
        fadeOut: Duration = Duration.ofMillis(500),
    ) {
        // MiniMessage の変換だけを担当し、
        // 実際の表示処理は Component 版へ委譲する
        showTitle(
            audience = audience,
            title = mini(title),
            subtitle = miniOrEmpty(subtitle),
            fadeIn = fadeIn,
            stay = stay,
            fadeOut = fadeOut,
        )
    }

    /**
     * MiniMessage 文字列からタイトルを作り、複数 Audience に表示する。
     *
     * @param audiences 表示先の集合。空の場合は何も表示しない。
     * @param title MiniMessage として解釈するメインタイトル文字列。
     * @param subtitle MiniMessage として解釈するサブタイトル文字列。空白のみなら表示しない。
     * @param fadeIn 表示開始時のフェード時間。
     * @param stay 表示を維持する時間。
     * @param fadeOut 表示終了時のフェード時間。
     */
    fun showMiniTitle(
        audiences: Iterable<Audience>,
        title: String,
        subtitle: String = "",
        fadeIn: Duration = Duration.ofMillis(500),
        stay: Duration = Duration.ofSeconds(1),
        fadeOut: Duration = Duration.ofMillis(500),
    ) {
        // 複数送信でも MiniMessage 版の入口を用意し、
        // 呼び出し側が Iterable を手動で回す必要をなくす
        showTitle(
            audiences = audiences,
            title = mini(title),
            subtitle = miniOrEmpty(subtitle),
            fadeIn = fadeIn,
            stay = stay,
            fadeOut = fadeOut,
        )
    }

    /**
     * MiniMessage 文字列を Component に変換する。
     *
     * @param message MiniMessage として解釈する文字列。
     * @return 変換後の Component。
     */
    private fun mini(message: String): Component {
        // MiniMessage インスタンスを object 内で共有し、
        // 変換処理の入口をこの関数に集約する
        return miniMessage.deserialize(message)
    }

    /**
     * 空白のみの MiniMessage 文字列を空 Component に変換する。
     *
     * @param message MiniMessage として解釈する文字列。空白のみの場合は非表示扱い。
     * @return 空白のみなら Component.empty、それ以外なら MiniMessage 変換後の Component。
     */
    private fun miniOrEmpty(message: String): Component {
        // 意図: サブタイトルにスペースや改行だけが渡された場合、
        //       見えない文字を表示枠として扱わない
        return if (message.isBlank()) {
            Component.empty()
        } else {
            miniMessage.deserialize(message)
        }
    }
}
