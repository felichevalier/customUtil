package com.example

import kotlin.random.Random

class WeightedRandom<T> {

    private val items = mutableListOf<Pair<T, Int>>()
    private var totalWeight: Int = 0

    /**
     * アイテムとその重みを追加します。
     * @param item 抽選対象のアイテム
     * @param weight アイテムの重み（1以上の整数）
     * @return 自身（連続追加が可能になります）
     */
    fun add(item: T, weight: Int): WeightedRandom<T> {
        require(weight > 0) { "Weight must be positive" }
        items.add(item to weight)
        totalWeight += weight
        return this
    }

    /**
     * 設定された重みに基づいて、ランダムにアイテムを抽選して返します。
     * @return 選ばれたアイテム
     */
    fun draw(): T {
        require(items.isNotEmpty()) { "No items available for drawing." }
        var randomValue = Random.nextInt(totalWeight)
        for ((item, weight) in items) {
            if (randomValue < weight) {
                return item
            }
            randomValue -= weight
        }
        // 論理上ここには到達しないはずです
        throw IllegalStateException("Unexpected error in draw()")
    }

    /**
     * Mapから WeightedRandom を生成するファクトリメソッドです。
     * Map の key がアイテム、value が重みとなります。
     */
    companion object {
        fun <T> fromMap(map: Map<T, Int>): WeightedRandom<T> {
            val weightedRandom = WeightedRandom<T>()
            map.forEach { (item, weight) ->
                weightedRandom.add(item, weight)
            }
            return weightedRandom
        }
    }
}