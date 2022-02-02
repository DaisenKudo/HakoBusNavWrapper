package io.github.qlain.hakobusnavwrapper.ui.main.buslocation.request

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
    ): View {
        this.busLocationRequestViewModel = ViewModelProvider(this)[BusLocationRequestViewModel::class.java]
        var etFrom = ""
        var etTo = ""

        return ComposeView(requireContext()).apply {
            setContent {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextField(
                        value = etFrom,
                        onValueChange = { etFrom = it },
                        label = { Text("乗車バス停") },
                        singleLine = true
                    )
                    Button(onClick = { //乗車バス停と降車バス停を入れ替えます
                        val tmp = etFrom
                        etFrom = etTo
                        etTo = tmp
                    }) {
                        Text(busLocationRequestViewModel.buttons["swap"]!!)
                    }
                    TextField(
                        value = etTo,
                        onValueChange = { etTo = it },
                        label = { Text(text = "降車バス停") },
                        singleLine = true
                    )
                    Button(onClick = {
                        from = "五稜郭"
                        to = "函館駅前"
                        //from = etFrom.text.toString()
                        //to = etTo.text.toString()
                        startActivity(Intent(activity, BusLocationResultActivity::class.java))
                    }) {
                        Text(busLocationRequestViewModel.buttons["confirm"]!!)
                    }
                }
            }
        }
    }

}