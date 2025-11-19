package ms.mattschlenkrich.paycalculator.ui.workdate.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.databinding.ListWorkDateTimeBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity

class WorkDateTimesAdapter(
    val mainActivity: MainActivity,
    private val mView: View,
) : RecyclerView.Adapter<WorkDateTimesAdapter.ViewHolder>() {

    val mainViewModel = mainActivity.mainViewModel
    val workOrderViewModel = mainActivity.workOrderViewModel
    private val df = DateFunctions()
    private val nf = NumberFunctions()

    class ViewHolder(val itemBinding: ListWorkDateTimeBinding) :
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
                        oldItem.workOrderHistory == newItem.workOrderHistory &&
                        oldItem.workDate == newItem.workDate
            }
        }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ListWorkDateTimeBinding.inflate(
                mainActivity.layoutInflater,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val history = differ.currentList[position]
        holder.itemBinding.apply {
            val tempStart = df.splitTimeFromDateTime(history.timeWorked.wohtStartTime)
            val startTime = df.get12HourDisplay("${tempStart[0]}:${tempStart[1]}")
            val tempEnd = df.splitTimeFromDateTime(history.timeWorked.wohtEndTime)
            val endTime = df.get12HourDisplay("${tempEnd[0]}:${tempEnd[1]}")
            var display = "$startTime  to $endTime"
            tvHours.text = display
            val hours =
                df.getTimeWorked(history.timeWorked.wohtStartTime, history.timeWorked.wohtEndTime)
            display = when (history.timeWorked.wohtTimeType) {
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
            tvHours.text = display
            tvWorkOrderNum.text = history.workOrderHistory.workOrder.woNumber
            holder.itemView.setOnClickListener {
                choosOptions(history)
            }
        }
    }

    private fun choosOptions(history: WorkOrderHistoryTimeWorkedCombined) {

    }
}