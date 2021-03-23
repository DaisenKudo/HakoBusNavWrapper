package io.github.qlain.hakobusnavwrapper.repository

import com.jakewharton.threetenabp.AndroidThreeTen
import io.github.qlain.hakobusnavwrapper.model.BusInformation
import okhttp3.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.threeten.bp.*
import java.io.IOException
import java.lang.NullPointerException
import java.lang.StringBuilder
import java.time.*

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
    private var data: BusInformation? = null

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
        //this.listener = listener

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                //todo:サーバエラーの可能性❓
                val doc = Jsoup.parse(response.body?.string())
                parse(doc)
                listener.onRefresh()
            }

            override fun onFailure(call: Call, e: IOException) {
                //TODO("Not yet implemented")
            }
        })

    }

    private fun parse(doc: Document): BusInformation {
        val results = ArrayList<BusInformation.Result>()

        fun isBusExist(): Boolean = try {
            doc.getElementById("errInfo").text().isEmpty()
        } catch (e: NullPointerException) {
            true
        }

        //データ取得日時
        //TODO:ABPのLocalTimeはよくないかも
        val refreshTime = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            java.time.LocalTime.parse(
                doc.body().getElementById("page-wrapper").getElementsByClass("center_box")[0].getElementsByClass("label_bar")[0].getElementsByClass("clear_fix")[0].getElementsByClass("col-md-6")[1].getElementsByClass("pull-right")[0].getElementsByTag("li")[2].text()
            )
        } else {
            val time = doc.body().getElementById("page-wrapper").getElementsByClass("center_box")[0].getElementsByClass("label_bar")[0].getElementsByClass("clear_fix")[0].getElementsByClass("col-md-6")[1].getElementsByClass("pull-right")[0].getElementsByTag("li")[2].text()

            org.threeten.bp.LocalTime.of(
                time.substringBefore(":").toInt(),
                time.substringAfter(":").toInt()
            )
        }

        /**
         * バスの一覧が入ったリスト群(未スクレイピング)
         */
        val list = if (isBusExist()) {
                doc.body()
                    .getElementById("page-wrapper")
                    .getElementsByClass("center_box")[0]
                    .getElementById("buslist")
                    .getElementsByClass("clearfix")[0]
                    .getElementsByClass("route_box")
            } else {
                null
            }



        list?.forEach {
            results.add(BusInformation.Result(

            ))
        }
    }
}