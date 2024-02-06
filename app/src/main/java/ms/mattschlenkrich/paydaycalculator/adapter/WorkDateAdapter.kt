package ms.mattschlenkrich.paydaycalculator.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.ListWorkDateBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkDates
import ms.mattschlenkrich.paydaycalculator.ui.paydays.TimeSheetFragmentDirections

class WorkDateAdapter(
    private val mainActivity: MainActivity,
    private val mView: View,
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
                return oldItem.workDateId == newItem.workDateId &&
                        oldItem.wdDate == newItem.wdDate
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
            if (display.isNotBlank()) display += " | "
            display += "${workDate.wdOtHours} Ot hrs"
        }
        if (workDate.wdDblOtHours > 0) {
            if (display.isNotBlank()) display += " | "
            display += "${workDate.wdDblOtHours} Dbl Ot hr"
        }
        if (workDate.wdStatHours > 0) {
            if (display.isNotBlank()) display += " | "
            display += "${workDate.wdStatHours} Stat or Vacation hrs"
        }
        holder.itemBinding.tvHours.text = display
        holder.itemView.setOnClickListener {
            AlertDialog.Builder(mView.context)
                .setTitle("Choose an action")
                .setItems(
                    arrayOf(
                        "Edit this date",
                        "Delete this date"
                    )
                ) { _, pos ->
                    when (pos) {
                        0 -> {
                            gotoWorkDateUpdate(workDate)
                        }

                        1 -> {
                            deleteWorkDate(workDate)
                        }

                        else -> {
                            //do nothing
                        }
                    }
                }
                .show()
        }
    }

    private fun deleteWorkDate(workDate: WorkDates) {
        mainActivity.payDayViewModel.updateWorkDate(
            WorkDates(
                workDate.workDateId,
                workDate.wdPayPeriodId,
                workDate.wdEmployerId,
                workDate.wdCutoffDate,
                workDate.wdDate,
                workDate.wdRegHours,
                workDate.wdOtHours,
                workDate.wdDblOtHours,
                workDate.wdStatHours,
                true,
                df.getCurrentTimeAsString()
            )
        )
    }

    private fun gotoWorkDateUpdate(workDate: WorkDates) {
        mainActivity.mainViewModel.setWorkDateObject(workDate)
        mView.findNavController().navigate(
            TimeSheetFragmentDirections
                .actionTimeSheetFragmentToWorkDateUpdateFragment()
        )
    }
}