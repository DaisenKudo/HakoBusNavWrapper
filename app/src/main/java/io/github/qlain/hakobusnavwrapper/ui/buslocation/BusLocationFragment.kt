package io.github.qlain.hakobusnavwrapper.ui.buslocation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.github.qlain.hakobusnavwrapper.R
import io.github.qlain.hakobusnavwrapper.repository.HakoBusLocationRepository

class BusLocationFragment : Fragment() {

    private lateinit var busLocationViewModel: BusLocationViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        busLocationViewModel =
                ViewModelProvider(this).get(BusLocationViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_bus_location, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        busLocationViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        HakoBusLocationRepository
            .from("函館駅前")
            .to("五稜郭")
            .build()
            .execute(busLocationViewModel)

        return root
    }
}