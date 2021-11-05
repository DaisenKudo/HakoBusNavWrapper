package io.github.qlain.hakobusnavwrapper.ui.buslocationresult.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.qlain.hakobusnavwrapper.R
import io.github.qlain.hakobusnavwrapper.model.BusInformation
import io.github.qlain.hakobusnavwrapper.repository.HakoBusLocationRepository
import io.github.qlain.hakobusnavwrapper.ui.main.buslocation.request.BusLocationRequestFragment

class BusLocationResultFragment : Fragment() {

    private lateinit var busLocationResultViewModel: BusLocationResultViewModel
    private var recyclerView: RecyclerView? = null
    private var viewAdapter: ViewAdapter = ViewAdapter(
        object : ViewAdapter.BusInformationViewListener {
            override fun onClickItem(tappedView: View, bus: BusInformation.Result) {
                //TODO("Not yet implemented")
            }
        }
    )
    private val hakoBusLocationRepository = HakoBusLocationRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        busLocationResultViewModel =
            ViewModelProvider(this).get(BusLocationResultViewModel::class.java)
        return inflater.inflate(R.layout.fragment_bus_location_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.recyclerView = view.findViewById<RecyclerView>(R.id.rv_bus_location_result)?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
            adapter = viewAdapter
        }

        busLocationResultViewModel.rv_bus_info.observe(viewLifecycleOwner, {
            viewAdapter.setBusList(it)
        })

        hakoBusLocationRepository
            .from(BusLocationRequestFragment.from)
            .to(BusLocationRequestFragment.to)
            .build()
            .execute(busLocationResultViewModel)
    }

    private class ViewAdapter(
        private val listener: BusInformationViewListener
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var busList: List<BusInformation.Result> = emptyList()
        fun setBusList(busList: List<BusInformation.Result>) {
            this.busList = if (busList.isNotEmpty()) {
                busList
            } else {
                //バスが60分以内にないとき
                //TODO:別なfragmentを表示させるべき
                listOf(BusInformation.Result("60分以内に接近しているバスはありません"))
            }
            notifyDataSetChanged()
        }

        interface BusInformationViewListener {
            fun onClickItem(tappedView: View, bus: BusInformation.Result)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.part_bus_location_result, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            holder.itemView.apply {
                findViewById<TextView>(R.id.tv_name).text = busList[position].name
                findViewById<TextView>(R.id.tv_estimate).text = if ( busList[position].estimate != null ) busList[position].estimate.toString() + "分" else ""
                findViewById<TextView>(R.id.tv_take).text = if ( busList[position].take != null ) busList[position].take.toString() + "分" else ""
                holder.itemView.setOnClickListener {
                    listener.onClickItem(it, busList[position])
                }
            }
        }

        override fun getItemCount(): Int = busList.size

    }

    private class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_name: TextView = itemView.findViewById(R.id.tv_name)
        val tv_estimate: TextView = itemView.findViewById(R.id.tv_estimate)
        val tv_take: TextView = itemView.findViewById(R.id.tv_take)
    }
}