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
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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
    private fun setURLParam(key: String, value: String) { param[key] = value }

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
    private fun reset() {
        //listener = null
        setURLParam("tabName", "searchTab")
        setURLParam("from", "")
        setURLParam("to", "")
        setURLParam("locale", "ja")
        setURLParam("bsid", "1")
    }

    /**
     * 乗るバス停を設定します
     */
    fun from(busStop: String): HakoBusLocationRepository {
        setURLParam("from", busStop)
        return this
    }

    /**
     * 降りるバス停を設定します
     */
    fun to(busStop: String): HakoBusLocationRepository {
        setURLParam("to", busStop)
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
        doc.body().getElementById("buslist")
                  .getElementsByClass("clearfix")[0]
                  .getElementsByClass("route_box")
                  ?.forEach { busList ->
            val name: String
            val via: String
            val direction: String
            val from: String
            val to: String
            val departure: BusInformation.Result.BusTime
            val arrive: BusInformation.Result.BusTime
            val take: Int
            val estimate: Int

            busList.getElementsByTag("table")[0].getElementsByTag("tbody")[0].getElementsByTag("tr").let {
                val timeRegex = Regex("[^0-9:-]")
                val timePattern = DateTimeFormatter.ofPattern("[]H:mm")

                //系統
                name = it[0].getElementsByTag("td")[0].getElementsByTag("span")[0].text()
                //経由
                via = it[1].getElementsByTag("td")[0].text()
                //方面 : <br>「車種：　ノンステップ」と言う文言が入ることがある
                direction = it[2].getElementsByTag("td")[0].text()
                //所要時間
                estimate = it[3].getElementsByTag("td")[0].text().replace(timeRegex, "").toInt()
                //乗り換え
                //it[3].getElementsByTag("td")[1].text()
                //乗車バス停
                from = it[4].getElementsByTag("td")[0].getElementsByTag("span")[1].text()
                //乗車時刻
                //時刻未定(--:--のときのExceptionである場合nullを入れる
                departure = BusInformation.Result.BusTime(
                    try { LocalTime.parse(it[5].getElementsByTag("td")[0].text().replace(timeRegex, ""), timePattern)} catch (e: DateTimeParseException){ null },
                    try { LocalTime.parse(it[5].getElementsByTag("td")[1].text().replace(timeRegex, ""), timePattern)} catch (e: DateTimeParseException){ null }
                )
                //降車バス停
                to = it[6].getElementsByTag("td")[0].getElementsByTag("span")[1].text()
                //降車時刻
                //時刻未定(--:--のときのExceptionである場合)nullを入れる
                arrive = BusInformation.Result.BusTime(
                    try { LocalTime.parse(it[7].getElementsByTag("td")[0].text().replace(timeRegex, ""), timePattern)} catch (e: DateTimeParseException){ null },
                    try { LocalTime.parse(it[7].getElementsByTag("td")[1].text().replace(timeRegex, ""), timePattern)} catch (e: DateTimeParseException){ null }
                )
                //バスが来るまでの時間
                take = if (it[8].getElementsByTag("td")[0].text() == "まもなく発車します") {
                    0
                } else {
                    it[8].getElementsByTag("td")[0].text().replace(timeRegex, "").toInt()
                }
            }
            results.add(BusInformation.Result(
                    name = name, via = via, direction = direction, from = from, to = to, departure = departure, arrive = arrive, take = take, estimate = estimate
            ))
        }

        Log.d("repo","data refreshed")

        this.busInformation = BusInformation(refTime, isBusExist(), results)

        return this
    }
}