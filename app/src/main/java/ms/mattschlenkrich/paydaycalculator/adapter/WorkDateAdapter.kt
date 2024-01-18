package ms.mattschlenkrich.paydaycalculator.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.ListWorkDateBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkDates

class WorkDateAdapter(
    private val mainActivity: MainActivity,
    mView: View,
) : RecyclerView.Adapter<WorkDateAdapter.WorkDateViewHolder>() {

    private val df = DateFunctions()

    class WorkDateViewHolder(val itemBinding: ListWorkDateBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<WorkDates>() {
            override fun areItemsTheSame(oldItem: WorkDates, newItem: WorkDates): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: WorkDates, newItem: WorkDates): Boolean {
                return oldItem.wdDate == newItem.wdDate &&
                        oldItem.wdEmployerId == newItem.wdEmployerId
            }
        }
    val differ = AsyncListDiffer(this, differCallBack)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkDateViewHolder {
        return WorkDateViewHolder(
            ListWorkDateBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: WorkDateViewHolder, position: Int) {
        val workDate = differ.currentList[position]
        var display = df.getDisplayDate(workDate.wdDate)
        if (workDate.wdIsDeleted) {
            display += " *Deleted*"
            holder.itemBinding.tvWorkDate.setTextColor(Color.RED)
        } else {
            holder.itemBinding.tvWorkDate.setTextColor(Color.BLACK)
        }
        holder.itemBinding.tvWorkDate.text = display
        display = ""
        if (workDate.wdRegHours > 0) {
            display = "${workDate.wdRegHours} Hrs"
        }
        if (workDate.wdOtHours > 0) {
            display += " ${workDate.wdOtHours} Ot"
        }
        if (workDate.wdDblOtHours > 0) {
            display += " ${workDate.wdDblOtHours} dbl Ot"
        }
        if (workDate.wdStatHours > 0) {
            display += " ${workDate.wdStatHours} Stat"
        }
        holder.itemBinding.tvHours.text = display
    }
}