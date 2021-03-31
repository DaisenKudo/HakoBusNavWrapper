package io.github.qlain.hakobusnavwrapper.ui.buslocation.results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.qlain.hakobusnavwrapper.R
import io.github.qlain.hakobusnavwrapper.model.BusInformation
import io.github.qlain.hakobusnavwrapper.repository.HakoBusLocationRepository

class BusLocationResultsFragment : Fragment() {

    private lateinit var busLocationResultsViewModel: BusLocationResultsViewModel
    private var resultRecyclerView: RecyclerView? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        busLocationResultsViewModel =
                ViewModelProvider(this).get(BusLocationResultsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_bus_location_request, container, false)
        //val textView: TextView = root.findViewById(R.id.bt_swap)

        val etFrom = root.findViewById<EditText>(R.id.et_from).apply {

        }
        val etTo = root.findViewById<EditText>(R.id.et_to).apply {

        }

        root.findViewById<Button>(R.id.bt_swap).apply {
            text = busLocationResultsViewModel.buttons["swap"]
            setOnClickListener {
                //乗車バス停と降車バス停を入れ替えます
                val tmp = etFrom.text
                etFrom.text = etTo.text
                etTo.text = tmp
            }
        }

        root.findViewById<Button>(R.id.bt_confirm).apply {
            text = busLocationResultsViewModel.buttons["confirm"]
            setOnClickListener {
                /**
                 * Not Implemented
                 *
                 * Resultsへ飛ばす
                 */
            }
        }

        this.resultRecyclerView = root.findViewById(R.id.recycler_view_results)
        this.resultRecyclerView?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
            adapter = BusLocationResultsAdapter(

            )
        }

        busLocationResultsViewModel.busData.observe(viewLifecycleOwner, Observer {

        })



        HakoBusLocationRepository
            .from("函館駅前")
            .to("五稜郭")
            .build()
            .execute(busLocationResultsViewModel)

        return root
    }
}

private class BusLocationResultsAdapter(
        private val list: List<BusInformation.Result>,
        private val listener: ResultsListListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface ResultsListListener {
        fun onClickItem(tappedView: View, result: BusInformation.Result)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val resultView: View = LayoutInflater.from(parent.context).inflate(R.layout.partview_bus_location_result, parent, false)
        return BusLocationResultsViewHolder(resultView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.tv_name).text = list[position].name
        holder.itemView.findViewById<TextView>(R.id.tv_via).text = list[position].via
        holder.itemView.findViewById<TextView>(R.id.tv_estimate).text = list[position].estimate.toString()
    }

    override fun getItemCount(): Int = list.size

    inner class BusLocationResultsViewHolder(resultView: View) : RecyclerView.ViewHolder(resultView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val tvVia: TextView = itemView.findViewById(R.id.tv_via)
        val tvEstimate: TextView = itemView.findViewById(R.id.tv_estimate)

    }
}