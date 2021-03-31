package io.github.qlain.hakobusnavwrapper.ui.buslocation.results

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.qlain.hakobusnavwrapper.model.BusInformation
import io.github.qlain.hakobusnavwrapper.repository.HakoBusLocationRepository
import java.time.LocalTime

class BusLocationResultsViewModel : ViewModel(), HakoBusLocationRepository.NotifyViewModel {

    val buttons = HashMap<String, String>().apply {
        this["swap"] = "↑↓"
        this["confirm"] = "検索"
    }

    private val _busData = MutableLiveData<BusInformation>().apply {
        value = BusInformation(LocalTime.now(), false, ArrayList())
    }
    val busData: LiveData<BusInformation> = _busData

    override fun onRefresh(data: BusInformation) {
        _busData.postValue(data)
    }

}