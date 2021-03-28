package io.github.qlain.hakobusnavwrapper.ui.buslocation.request

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.github.qlain.hakobusnavwrapper.R
import io.github.qlain.hakobusnavwrapper.repository.HakoBusLocationRepository

class BusLocationRequestFragment : Fragment() {

    private lateinit var busLocationRequestViewModel: BusLocationRequestViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        busLocationRequestViewModel =
                ViewModelProvider(this).get(BusLocationRequestViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_bus_location_request, container, false)
        //val textView: TextView = root.findViewById(R.id.bt_swap)

        val etFrom = root.findViewById<EditText>(R.id.et_from).apply {

        }
        val etTo = root.findViewById<EditText>(R.id.et_to).apply {

        }

        root.findViewById<Button>(R.id.bt_swap).apply {
            text = busLocationRequestViewModel.buttons["swap"]
            setOnClickListener {
                //乗車バス停と降車バス停を入れ替えます
                val tmp = etFrom.text
                etFrom.text = etTo.text
                etTo.text = tmp
            }
        }

        /*busLocationViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })*/

        HakoBusLocationRepository
            .from("函館駅前")
            .to("五稜郭")
            .build()
            .execute(busLocationRequestViewModel)

        return root
    }
}