package ms.mattschlenkrich.paycalculator.ui.workorder.area.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paycalculator.database.model.workorder.Areas
import ms.mattschlenkrich.paycalculator.databinding.ListSingleItemBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.workorder.area.AreaViewFragment

class AreaViewAdapter(
    private val mainActivity: MainActivity,
    private val mView: View,
    private val areaViewFragment: AreaViewFragment,
    private val parentTag: String,
) : RecyclerView.Adapter<AreaViewAdapter.AreaViewHolder>() {

    class AreaViewHolder(val itemBinding: ListSingleItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<Areas>() {
            override fun areItemsTheSame(oldItem: Areas, newItem: Areas): Boolean {
                return oldItem.areaId == newItem.areaId &&
                        oldItem.areaName == newItem.areaName
            }

            override fun areContentsTheSame(oldItem: Areas, newItem: Areas): Boolean {
                return oldItem == newItem
            }
        }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AreaViewHolder {
        return AreaViewHolder(
            ListSingleItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: AreaViewHolder, position: Int) {
        val area = differ.currentList[position]
        holder.itemBinding.apply {
            var display = area.areaName
            if (area.areaIsDeleted) {
                display += " *DELETED*"
                tvDisplay.setTextColor(Color.RED)
            } else {
                tvDisplay.setTextColor(Color.BLACK)
            }
            tvDisplay.text = display
        }
        holder.itemView.setOnClickListener {
            gotoAreaUpdate(area.areaId)
        }
    }

    private fun gotoAreaUpdate(areaId: Long) {
        mainActivity.mainViewModel.setCallingFragment(parentTag)
        mainActivity.mainViewModel.setAreaId(areaId)
        areaViewFragment.gotoAreaUpdateFragment()
    }
}
