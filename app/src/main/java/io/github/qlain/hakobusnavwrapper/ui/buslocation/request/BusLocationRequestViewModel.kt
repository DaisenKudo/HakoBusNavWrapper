package io.github.qlain.hakobusnavwrapper.ui.buslocation.request

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.qlain.hakobusnavwrapper.model.BusInformation
import io.github.qlain.hakobusnavwrapper.repository.HakoBusLocationRepository

class BusLocationRequestViewModel : ViewModel(), HakoBusLocationRepository.NotifyViewModel {

    val buttons = HashMap<String, String>().apply {
        this["swap"] = "↑↓"
        this["confirm"] = "検索"
    }

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    override fun onRefresh(data: BusInformation) {
        _text.postValue(data.isBusExist.toString())
    }

}