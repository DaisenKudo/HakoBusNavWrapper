package io.github.qlain.hakobusnavwrapper.ui.buslocation.result

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.qlain.hakobusnavwrapper.model.BusInformation
import io.github.qlain.hakobusnavwrapper.repository.HakoBusLocationRepository

class BusLocationResultViewModel : ViewModel(), HakoBusLocationRepository.NotifyViewModel {

    private val _rv_bus_info = MutableLiveData<List<BusInformation.Result>>().apply {
        value = listOf()
    }
    val rv_bus_info: LiveData<List<BusInformation.Result>> = _rv_bus_info

    override fun onRefresh(data: BusInformation) {
        _rv_bus_info.postValue(data.results)
    }

}