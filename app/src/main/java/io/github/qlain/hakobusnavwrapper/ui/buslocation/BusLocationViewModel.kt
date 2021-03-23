package io.github.qlain.hakobusnavwrapper.ui.buslocation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.qlain.hakobusnavwrapper.model.BusInformation
import io.github.qlain.hakobusnavwrapper.repository.HakoBusLocationRepository

class BusLocationViewModel : ViewModel(), HakoBusLocationRepository.NotifyViewModel {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    override fun onRefresh(data: BusInformation) {
        _text.value = data
    }

}