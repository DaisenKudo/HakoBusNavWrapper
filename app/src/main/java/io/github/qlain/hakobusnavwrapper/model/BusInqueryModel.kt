package io.github.qlain.hakobusnavwrapper.model

data class BusInqueryModel(
    val tabName: String = "searchTab",
    var from: String = "",
    var to: String = "",
    var locale: String = "ja",
)