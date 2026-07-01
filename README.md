# customUtil

`customUtil` は Paper プラグイン開発で使う自分用 Kotlin ユーティリティです。

主な用途は以下です。

- Adventure `Audience` 向けのメッセージ・タイトル送信
- オンラインプレイヤーなど複数人向け送信対象の取得
- BossBar 付きカウントダウンタイマー
- スポーン地点へ戻る「戻り棒」アイテム
- 重み付きランダム抽選

## Requirements

- Java 21
- Paper API `1.21.11`
- Kotlin JVM `1.9.23`
- Gradle wrapper 同梱

このリポジトリの Gradle / Kotlin DSL は Java 25 では失敗するため、ビルド時は JDK 21 を指定します。

```sh
JAVA_HOME=/Users/grandfather/Library/Java/JavaVirtualMachines/openjdk-21.0.2/Contents/Home PATH=/usr/bin:/bin:/usr/sbin:/sbin ./gradlew build
```

開発用に shadow jar を作る場合は `isDev=true` を指定します。

```sh
JAVA_HOME=/Users/grandfather/Library/Java/JavaVirtualMachines/openjdk-21.0.2/Contents/Home PATH=/usr/bin:/bin:/usr/sbin:/sbin ./gradlew shadowJar -PisDev=true
```

## Plugin

`src/main/resources/plugin.yml` の設定は以下です。

```yaml
name: CustomUtil
version: '2.0'
main: com.custom.minecraft.test.Test
api-version: '1.21.11'
```

現在の `Test` プラグインには動作確認用コマンドがあります。

```text
/testTimer set <hour> <minute> <second>
/testTimer stop
/testTimer reset
/test
```

`/testTimer set` は BossBar 付きタイマーを開始します。`/test` は実行プレイヤーに戻り棒を付与します。

## Message API

通知処理は `Messenger`、送信対象の取得は `AudienceResolver` を使います。

旧 `ChatMessage` / `TitleMessage` は削除済みです。状態を持つ Builder API は使わず、名前付き引数の関数 API に統一しています。

### 通常テキストを送る

```kotlin
import com.custom.minecraft.message.Messenger
import net.kyori.adventure.text.format.NamedTextColor

Messenger.sendText(
    audience = player,
    message = "ゲーム開始",
    color = NamedTextColor.GREEN,
)
```

### 複数人へ通常テキストを送る

```kotlin
import com.custom.minecraft.message.AudienceResolver
import com.custom.minecraft.message.Messenger
import net.kyori.adventure.text.format.NamedTextColor

Messenger.sendText(
    audiences = AudienceResolver.allOnline(),
    message = "全員に表示されます",
    color = NamedTextColor.GOLD,
)
```

### MiniMessage を送る

```kotlin
import com.custom.minecraft.message.Messenger

Messenger.sendMini(
    audience = player,
    message = "<green>あなたにだけ表示されます</green>",
)
```

### MiniMessage タイトルを表示する

```kotlin
import com.custom.minecraft.message.AudienceResolver
import com.custom.minecraft.message.Messenger
import java.time.Duration

Messenger.showMiniTitle(
    audiences = AudienceResolver.allOnline(),
    title = "<gold><bold>GAME START</bold></gold>",
    subtitle = "<gray>準備してください</gray>",
    stay = Duration.ofSeconds(3),
)
```

### Component タイトルを表示する

```kotlin
import com.custom.minecraft.message.Messenger
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

Messenger.showTitle(
    audience = player,
    title = Component.text("ROUND 1", NamedTextColor.GOLD),
    subtitle = Component.text("Start!", NamedTextColor.GRAY),
)
```

### 送信対象を取得する

```kotlin
AudienceResolver.allOnline()
AudienceResolver.sameWorld(player)
AudienceResolver.operators()
AudienceResolver.permission("customutil.notice")
```

個人送信では `AudienceResolver` を使わず、`player` を直接 `Messenger` に渡します。

## Timer

`MinecraftTimer` は BossBar で残り時間を表示するカウントダウンタイマーです。

- 内部カウントはすべて秒単位
- 表示形式は常に `0h0m20s` / `1h20m59s`
- `TimerListener` に `TimerEvent` を通知
- 開始・終了タイトルの表示可否を設定可能
- 基本操作は `start()` / `stop()` / `reset()`
- 状態は `Idle` / `Running` / `Finished`
- 一時停止機能は存在しない

再開始するときは `reset()` で `Idle` に戻してから `start()` を呼びます。`Running` 中の `start()` は `false` を返します。

### 基本例

```kotlin
import com.custom.minecraft.timer.MinecraftTimer
import com.custom.minecraft.timer.domain.TimerEvent
import com.custom.minecraft.timer.listener.TimerListener
import org.bukkit.plugin.java.JavaPlugin

class MyPlugin : JavaPlugin(), TimerListener {

    private lateinit var timer: MinecraftTimer

    fun startGameTimer() {
        timer = MinecraftTimer(this, this)
            .setDuration(hours = 0, minutes = 1, seconds = 30)
            .setTargets(server.onlinePlayers)
            .setTitle(start = "スタート", finish = "終了")
            .timerTitleVisibility(true)

        timer.start()
    }

    override fun onEvent(event: TimerEvent) {
        when (event) {
            is TimerEvent.Started -> logger.info("started: ${event.totalSeconds}")
            is TimerEvent.Tick -> logger.info("remaining: ${event.remainingSeconds}")
            is TimerEvent.Finished -> logger.info("finished: ${event.reason}")
        }
    }
}
```

### 制御

```kotlin
val started = timer.start()
val stopped = timer.stop()

timer.reset()
val restarted = timer.start()
```

- `start()`: `Idle` / `Finished` から開始し、成功時は `true`。開始時に残り時間を設定値へ戻します。
- `stop(reason)`: `Running` のときだけ停止し、成功時は `true`。既定理由は `STOPPED` です。
- `reset()`: タスクと BossBar を削除し、残り時間と状態を初期化します。終了イベントは通知しません。

### 時間設定

```kotlin
timer.setDuration(hours = 1, minutes = 20, seconds = 30)
```

時間、分、秒は0以上を指定します。`Running` 中に時間を変更すると `IllegalStateException` が発生します。

### 対象プレイヤー管理

対象は内部で UUID として保持され、表示時にオンラインプレイヤーだけが解決されます。

```kotlin
timer.setTargets(server.onlinePlayers) // 既存対象を置き換える
timer.addTarget(player)
timer.addTargets(players)
timer.removeTarget(player)
timer.clearTargets()
```

時間、対象、BossBar、開始・終了メッセージの設定は `Running` 中に変更できません。

### TimerEvent

- `Started(timerId, totalSeconds)`: 開始成功時に通知
- `Tick(timerId, remainingSeconds)`: 1秒ごとの更新時に通知
- `Finished(timerId, reason)`: 停止または時間切れ時に1回通知

### FinishReason

- `COMPLETED`: 時間切れ
- `STOPPED`: 通常停止
- `CANCELLED`: ゲーム中断やプラグイン終了

## Return Stick

`ReturnStick` は `CARROT_ON_A_STICK` に表示名 `戻り棒` を付け、使用時に通常ワールドのスポーン地点へ戻すユーティリティです。

### 初期化と付与

```kotlin
import com.custom.minecraft.item.returnstick.ReturnStick

private lateinit var returnStick: ReturnStick

override fun onEnable() {
    returnStick = ReturnStick().init(this)
}

fun give(player: Player) {
    returnStick.giveStick(player)
}
```

### 複数人へ付与

```kotlin
returnStick.giveStick(server.onlinePlayers)
```

### 挙動

- 戻り棒を右クリックすると通常ワールドのスポーン地点へテレポート
- 死亡時に戻り棒がドロップに含まれている場合、ドロップから除外
- リスポーン時に戻り棒を再付与

## Lottery

`Lottery<T>` は重み付きランダム抽選を行うユーティリティです。

### 連続追加

```kotlin
import com.custom.minecraft.lottery.Lottery

val reward = Lottery<String>()
    .add("common", 80)
    .add("rare", 15)
    .add("legendary", 5)
    .draw()
```

### Map から作成

```kotlin
val lottery = Lottery.fromMap(
    mapOf(
        "stone" to 70,
        "iron" to 25,
        "diamond" to 5,
    )
)

val item = lottery.draw()
```

`weight` は 1 以上である必要があります。抽選対象が空の場合、`draw()` は例外を投げます。

## Other Utilities

### Tool

`Tool` はツール系 `Material` の一覧を保持するためのオブジェクトです。現時点では外部公開 API として使う関数はありません。

### CustomDialogBuilder

`CustomDialogBuilder` は現時点では空実装です。ダイアログ API の土台として配置されています。

## Migration

`ChatMessage` / `TitleMessage` は削除済みです。以下の API に置き換えてください。

### ChatMessage からの移行

```kotlin
Messenger.sendText(
    audience = player,
    message = "ゲーム開始",
    color = NamedTextColor.GREEN,
)
```

### TitleMessage からの移行

```kotlin
Messenger.showMiniTitle(
    audience = player,
    title = "<gold><bold>ROUND 1</bold></gold>",
    subtitle = "<gray>Start!</gray>",
)
```

## Notes

- `Messenger` は送信対象の判定を行いません。対象の取得は `AudienceResolver` または呼び出し元で行います。
- `AudienceResolver` は現時点では `Bukkit.getOnlinePlayers()` を直接使います。
- `Timer` の表示は `h/m/s` を常に含みます。例: `0h0m20s`
- `plugin.yml` のバージョンは `2.0`、Gradle の project version は `1.0.0` です。
