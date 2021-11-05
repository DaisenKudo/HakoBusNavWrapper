package io.github.qlain.hakobusnavwrapper.model

import java.time.LocalTime

data class BusInformation(
    val refTime: LocalTime, //データ取得日時
    val isBusExist: Boolean, //データ取得時点から60分以内にバスがあるか(60分以上は取得元の制約で取得できないことがある)
    val results: List<Result> //結果一覧(isBusExistがfalseなら空リストになるべき)
) {
    data class Result(
        val name: String, //バス系統名
        val via: String? = null, //経由
        val direction: String? = null, //バスの目的地
        val from: String? = null, //乗車バス停
        val to: String? = null, //降車バス停
        val departure: BusTime? = null, //バス発車
        val arrive: BusTime? = null, //バス到着
        val take: Int? = null, //予想乗車時間
        val estimate: Int? = null //あと何分後にバスが来るか
    ) {
        data class BusTime(
            val schedule: LocalTime?, //定刻(--:--の場合はnull)
            val prediction: LocalTime? //予測(--:--の場合はnull)
        ) {
            //定刻からの遅れ(マイナスの場合は早い)
            //日付をまたぐような遅れには未対応
            fun delayed(): Int? {
                return if (this.schedule != null && this.prediction != null) {
                    val p = this.prediction.hour * 60 + this.prediction.minute
                    val s = this.schedule.hour * 60 + this.schedule.minute

                    p - s
                } else {
                    null
                }
            }

        }
    }
}