package io.github.qlain.hakobusnavwrapper.model

import java.time.LocalTime

data class BusInformation(
    val refDate: LocalTime, //データ取得日時
    val results: ArrayList<Result> //結果一覧
) {
    data class Result(
        val name: String, //バス系統名
        val from: String, //乗車バス停
        val via: String, //経由
        val to: String, //降車バス停
        val departure: BusTime, //バス発車
        val arrive: BusTime, //バス到着
        val take: Int, //予想乗車時間
        val estimate: Int //あと何分後にバスが来るか
    ) {
        data class BusTime(
            val schedule: LocalTime, //定刻
            val prediction: LocalTime, //予測
            val delayed: Int, //定刻からの遅れ(マイナスの場合は早い)
        )
    }
}