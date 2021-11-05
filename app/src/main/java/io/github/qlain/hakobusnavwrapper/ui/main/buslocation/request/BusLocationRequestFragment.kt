package io.github.qlain.hakobusnavwrapper.ui.main.buslocation.request

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.github.qlain.hakobusnavwrapper.R
import io.github.qlain.hakobusnavwrapper.ui.buslocationresult.BusLocationResultActivity

class BusLocationRequestFragment : Fragment() {

    private lateinit var busLocationRequestViewModel: BusLocationRequestViewModel

    companion object {
        var from = ""
        var to = ""
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        busLocationRequestViewModel =
                ViewModelProvider(this).get(BusLocationRequestViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_bus_location_request, container, false)
        //val textView: TextView = root.findViewById(R.id.bt_swap)

        val etFrom = root.findViewById<EditText>(R.id.tv_from).apply {

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

        root.findViewById<Button>(R.id.bt_confirm).apply {
            text = busLocationRequestViewModel.buttons["confirm"]
            setOnClickListener {
                from = "五稜郭"
                to = "函館駅前"
                /*from = etFrom.text.toString()
                to = etTo.text.toString()*/
                startActivity(Intent(activity, BusLocationResultActivity::class.java))
            }
        }

        return root
    }
}