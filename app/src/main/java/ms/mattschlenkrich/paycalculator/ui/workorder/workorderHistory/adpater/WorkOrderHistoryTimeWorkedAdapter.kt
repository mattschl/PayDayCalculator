package ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory.adpater

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.databinding.ListSingleItemBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory.WorkOrderHistoryTimeFragment

class WorkOrderHistoryTimeWorkedAdapter(
    val mainActivity: MainActivity,
    private val mView: View,
    private val parentFragment: String,
    private val workOrderHistoryTimeFragment: WorkOrderHistoryTimeFragment
) : RecyclerView.Adapter<WorkOrderHistoryTimeWorkedAdapter.ViewHolder>() {

    val mainViewModel = mainActivity.mainViewModel
    val workOrderViewModel = mainActivity.workOrderViewModel
    private val df = DateFunctions()
    private val nf = NumberFunctions()

    class ViewHolder(val itemBinding: ListSingleItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallback =
        object : DiffUtil.ItemCallback<WorkOrderHistoryTimeWorkedCombined>() {
            override fun areItemsTheSame(
                oldItem: WorkOrderHistoryTimeWorkedCombined,
                newItem: WorkOrderHistoryTimeWorkedCombined
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: WorkOrderHistoryTimeWorkedCombined,
                newItem: WorkOrderHistoryTimeWorkedCombined
            ): Boolean {
                return oldItem.timeWorked == newItem.timeWorked &&
                        oldItem.workDate == newItem.workDate &&
                        oldItem.workOrderHistory == newItem.workOrderHistory
            }
        }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ListSingleItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val history = differ.currentList[position]
        holder.itemBinding.apply {
            val tempStart = df.splitTimeFromDateTime(history.timeWorked.wohtStartTime)
            val startTime = df.get12HourDisplay("${tempStart[0]}:${tempStart[1]}")
            val tempEnd = df.splitTimeFromDateTime(history.timeWorked.wohtEndTime)
            val endTime = df.get12HourDisplay("${tempEnd[0]}:${tempEnd[1]}")
            var display = "$startTime  to $endTime"
            val hours =
                df.getTimeWorked(history.timeWorked.wohtStartTime, history.timeWorked.wohtEndTime)

            display += when (history.timeWorked.wohtTimeType) {
                1 -> " - ${mView.context.getString(R.string.reg_hrs_)}${nf.getNumberFromDouble(hours)}"
                2 -> " - ${mView.context.getString(R.string.ot_hrs_)}${nf.getNumberFromDouble(hours)}"
                3 -> " - ${mView.context.getString(R.string.dblot_hrs_)}${
                    nf.getNumberFromDouble(
                        hours
                    )
                }"

                else -> {
                    " Break time"
                }
            }
            tvDisplay.text = display
            holder.itemView.setOnClickListener {
                choosOptions(history)
            }
        }
    }

    private fun choosOptions(history: WorkOrderHistoryTimeWorkedCombined) {
        AlertDialog.Builder(mView.context)
            .setTitle(
                "${mView.context.getString(R.string.choose_option_for_wo)} ${history.workOrderHistory.workOrder.woNumber}${
                    mView.context.getString(
                        R.string._on_
                    )
                } ${df.getDisplayDate(history.workDate.wdDate)}"
            )
            .setItems(
                arrayOf(
                    mView.context.getString(R.string.edit),
                    mView.context.getString(R.string.delete)
                )
            ) { _, position ->
                when (position) {
                    0 -> gotoWorkOrderHistoryTimeEdit(history)
                    1 -> deleteTimeWorked(history)
                }
            }
            .setNegativeButton(mView.context.getString(R.string.cancel), null)
            .show()
    }

    private fun deleteTimeWorked(history: WorkOrderHistoryTimeWorkedCombined) {
        workOrderViewModel.deleteTimeWorked(history.timeWorked)
    }

    private fun gotoWorkOrderHistoryTimeEdit(history: WorkOrderHistoryTimeWorkedCombined) {
        mainViewModel.setWorkOrderHistoryTimeWorkedCombined(history)
        workOrderHistoryTimeFragment.gotoWorkOrderHistoryTimeUpdateFragment()
    }
}