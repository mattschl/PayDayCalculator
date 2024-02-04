package ms.mattschlenkrich.paydaycalculator.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.ListWorkDateBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkDateAndExtras
import ms.mattschlenkrich.paydaycalculator.ui.paydays.TimeSheetFragmentDirections

class WorkDateAdapter(
    private val mainActivity: MainActivity,
    private val mView: View,
) : RecyclerView.Adapter<WorkDateAdapter.WorkDateViewHolder>() {

    private val df = DateFunctions()

    class WorkDateViewHolder(val itemBinding: ListWorkDateBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<WorkDateAndExtras>() {
            override fun areItemsTheSame(
                oldItem: WorkDateAndExtras,
                newItem: WorkDateAndExtras
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: WorkDateAndExtras,
                newItem: WorkDateAndExtras
            ): Boolean {
                return oldItem.workDate.wdEmployerId == newItem.workDate.wdEmployerId &&
                        oldItem.workDate.wdDate == newItem.workDate.wdDate
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
        val workDateAndExtras = differ.currentList[position]
        var display = df.getDisplayDate(workDateAndExtras.workDate.wdDate)
        if (workDateAndExtras.workDate.wdIsDeleted) {
            display += " *Deleted*"
            holder.itemBinding.tvWorkDate.setTextColor(Color.RED)
        } else {
            holder.itemBinding.tvWorkDate.setTextColor(Color.BLACK)
        }
        holder.itemBinding.tvWorkDate.text = display
        display = ""
        if (workDateAndExtras.workDate.wdRegHours > 0) {
            display = "${workDateAndExtras.workDate.wdRegHours} Hrs"
        }
        if (workDateAndExtras.workDate.wdOtHours > 0) {
            if (display.isNotBlank()) display += " | "
            display += "${workDateAndExtras.workDate.wdOtHours} Ot hrs"
        }
        if (workDateAndExtras.workDate.wdDblOtHours > 0) {
            if (display.isNotBlank()) display += " | "
            display += "${workDateAndExtras.workDate.wdDblOtHours} Dbl Ot hr"
        }
        if (workDateAndExtras.workDate.wdStatHours > 0) {
            if (display.isNotBlank()) display += " | "
            display += "${workDateAndExtras.workDate.wdStatHours} Stat or Vacation hrs"
        }
        holder.itemBinding.tvHours.text = display
        holder.itemView.setOnClickListener {
            gotoWorkDateUpdate(workDateAndExtras)
        }
    }

    private fun gotoWorkDateUpdate(workDateAndExtras: WorkDateAndExtras) {
        mainActivity.mainViewModel.setWorkDateObject(workDateAndExtras.workDate)
        mView.findNavController().navigate(
            TimeSheetFragmentDirections
                .actionTimeSheetFragmentToWorkDateUpdateFragment()
        )
    }
}