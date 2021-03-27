package io.github.qlain.hakobusnavwrapper.repository

import android.util.Log
import io.github.qlain.hakobusnavwrapper.model.BusInformation
import okhttp3.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.lang.IllegalStateException
import java.lang.NullPointerException
import java.lang.StringBuilder
import java.time.LocalTime

//函館バスロケーション情報取得用のURL
private const val URI = "https://hakobus.bus-navigation.jp/wgsys/wgs/bus.htm"

object HakoBusLocationRepository {
    /**
     * データを受け取るViewModelにリッスンさせます
     */
    interface NotifyViewModel {
        fun onRefresh(data: BusInformation)
    }
    private var listener: NotifyViewModel? = null

    /**
     * 例
     * https://hakobus.bus-navigation.jp/wgsys/wgp/bus.htm?tabName=searchTab&selectedLandmarkCatCd=&selectfiftySoundCharacter=&from=%E4%BA%94%E7%A8%9C%E9%83%AD&fromType=&to=%E5%87%BD%E9%A4%A8%E9%A7%85%E5%89%8D&toType=&locale=ja&fromlat=&fromlng=&tolat=&tolng=&fromSignpoleKey=&routeLayoutCd=&bsid=1&fromBusStopCd=&toBusStopCd=&mapFlag=false&existYn=N&routeKey=&nextDiagramFlag=0&diaRevisedDate=
     */
    private val param = HashMap<String, String>()
    private fun setParam(key: String, value: String) { param[key] = value }

    /**
     * OkHttp3
     */
    private val client = OkHttpClient.Builder().build()
    private var request: Request? = null

    /**
     * パースされたデータ
     */
    private var busInformation: BusInformation? = null

    init {
        reset()
    }

    /**
     * URIパラメータの初期値を設定・再設定します
     */
    fun reset() {
        //listener = null
        setParam("tabName", "searchTab")
        setParam("selectedLandmarkCatCd", "")
        setParam("selectfiftySoundCharacter", "")
        setParam("from", "")
        setParam("fromType", "1")
        setParam("to", "")
        setParam("toType", "")
        setParam("locale", "ja")
        setParam("fromlat", "")
        setParam("fromlng", "")
        setParam("tolat", "")
        setParam("tolng", "")
        setParam("fromSignpoleKey", "")
        setParam("routeLayoutCd", "")
        setParam("bsid", "1")
        setParam("fromBusStopCd", "")
        setParam("toBusStopCd", "")
        setParam("mapFlag", "false")
        setParam("existYn", "")
    }

    /**
     * 乗るバス停を設定します
     */
    fun from(busStop: String): HakoBusLocationRepository {
        setParam("from", busStop)
        return this
    }

    /**
     * 降りるバス停を設定します
     */
    fun to(busStop: String): HakoBusLocationRepository {
        setParam("to", busStop)
        return this
    }

    /**
     * ナビゲーションシステムへアクセスするためのURLを生成します
     */
    fun build(): HakoBusLocationRepository {
        val sb = StringBuilder(450)

        //URIパラメータとして文字列生成
        sb.append(URI, "?")
        param.forEach { (key, value) ->
            sb.append(key, "=", value, "&")
        }
        //末尾につく'&'を削除
        sb.deleteCharAt(sb.length - 1)

        request = Request.Builder().url(sb.toString()).get().build()
        return this
    }

    /**
     * 実際にナビゲーションシステムにアクセスしWebページの内容を返します
     */
    fun execute(listener: NotifyViewModel) {
        val request = requireNotNull(request)
        this.listener = listener

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                //todo:サーバエラーの可能性❓
                val doc = Jsoup.parse(response.body?.string())
                parse(doc)
                listener.onRefresh(busInformation ?: throw IllegalStateException())
            }

            override fun onFailure(call: Call, e: IOException) {
                //TODO("Not yet implemented")
            }
        })

    }

    /**
     * 取得したWebページを実際にパースします(重そう)
     */
    private fun parse(doc: Document): HakoBusLocationRepository {
        val results = ArrayList<BusInformation.Result>()

        fun isBusExist(): Boolean = try {
            doc.getElementById("errInfo").text().isEmpty()
        } catch (e: NullPointerException) {
            true
        }

        //データ取得日時
        val refTime = LocalTime.parse(
                doc.body().getElementsByClass("container")[0].getElementsByClass("label_bar")[0].getElementsByClass("clearfix")[0].getElementsByTag("ul")[0].getElementsByTag("li")[1].text()
        )

        /**
         * バスがない場合、これ以上のスクレイピングを行わずに結果を返す
         */
        if (!isBusExist()) {
            this.busInformation = BusInformation(refTime, isBusExist(), ArrayList())
            return this
        }

        /**
         * バスの一覧が入ったリスト群(未スクレイピング)
         */
        doc.body().getElementById("page-wrapper")
                  .getElementsByClass("center_box")[0]
                  .getElementById("buslist")
                  .getElementsByClass("clearfix")[0]
                  .getElementsByClass("route_box")
                  ?.forEach { busList ->
            val name: String
            val via: String
            val from: String
            val to: String
            var schedule: LocalTime
            var prediction: LocalTime
            var delayed: Int
            val departure: BusInformation.Result.BusTime
            val arrive: BusInformation.Result.BusTime
            val take: Int
            val estimate: Int

            busList.getElementsByTag("table")[0].getElementsByTag("tbody")[0].getElementsByTag("tr").let { it1 ->
                it1[0].let { it2 ->
                    it2.getElementsByTag("td").let {
                        it[0].getElementsByTag("table")[0].getElementsByTag("tbody")[0].getElementsByTag("tr").let {
                            name = it[0].text() //バス系統名
                            via = it[2].text() //経由
                        }
                        //it[1] //no data
                        it[2].getElementsByTag("div")[0].getElementsByTag("table")[0].getElementsByTag("tbody")[0].getElementsByTag("tr").let {
                            //乗車バス停
                            from = it[0].getElementsByTag("font")[0].text()
                            //定刻
                            schedule = LocalTime.parse(it[1].text().filter { Regex("[0-9:]").containsMatchIn(it.toString()) })
                            //予測
                            prediction = LocalTime.parse(it[2].text().filter { Regex("[0-9:]").containsMatchIn(it.toString()) })
                            //定刻からの遅れ(マイナスの場合は早い、日付跨ぎを想定していない)
                            delayed = (prediction.hour - schedule.hour) * 60 + (prediction.minute - schedule.minute)
                            //バス発車
                            departure = BusInformation.Result.BusTime(schedule, prediction, delayed)
                        }
                        it[3].getElementsByTag("div")[0].getElementsByTag("table")[0].getElementsByTag("tbody")[0].getElementsByTag("tr").let {
                            //乗車バス停
                            to = it[0].getElementsByTag("font")[0].text()
                            //定刻
                            schedule = LocalTime.parse(it[1].text().filter { Regex("[0-9:]").containsMatchIn(it.toString()) })
                            //予測
                            prediction = LocalTime.parse(it[2].text().filter { Regex("[0-9:]").containsMatchIn(it.toString()) })
                            //定刻からの遅れ(マイナスの場合は早い、日付跨ぎを想定していない)
                            delayed = (prediction.hour - schedule.hour) * 60 + (prediction.minute - schedule.minute)
                            //バス発車
                            arrive = BusInformation.Result.BusTime(schedule, prediction, delayed)
                        }
                        it[4].getElementsByTag("div")[0].getElementsByTag("table")[0].getElementsByTag("tbody")[0].getElementsByTag("tr").let {
                            take = it[1].text().filter { Regex("0-9").containsMatchIn(it.toString()) }.toInt()
                        }
                    }
                }
                it1[1].let {
                    //あと何分後にバスが来るか
                    val t = it.getElementsByTag("td")[1].text().filter { Regex("0-9").containsMatchIn( it.toString()) }.toInt()
                    estimate = if (t != 0) t else 0
                    //it.getElementsByTag("td")[2]で乗り換え情報取ってこれそう
                }
            }
            results.add(BusInformation.Result(
                    name, from, via, to, departure, arrive, take, estimate
            ))
        }

        Log.d("repo","data refreshed")

        this.busInformation = BusInformation(refTime, isBusExist(), results)

        return this
    }
}