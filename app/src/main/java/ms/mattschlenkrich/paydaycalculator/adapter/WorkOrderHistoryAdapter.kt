package ms.mattschlenkrich.paydaycalculator.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.ListWorkOrderHistoryItemBinding
import ms.mattschlenkrich.paydaycalculator.model.workOrder.WorkOrderHistory
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import ms.mattschlenkrich.paydaycalculator.ui.workOrder.TimeSheetAddWorkOrderFragment

class WorkOrderHistoryAdapter(
    val mainActivity: MainActivity,
    val mView: View,
    private val parentFragment: TimeSheetAddWorkOrderFragment,
    private val workOrderHistory: ArrayList<WorkOrderHistory>
) : RecyclerView.Adapter<WorkOrderHistoryAdapter.ViewHolder>() {

    private val df = DateFunctions()
    private val nf = NumberFunctions()

    class ViewHolder(
        val itemBinding: ListWorkOrderHistoryItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListWorkOrderHistoryItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val history = workOrderHistory[position]
        holder.itemBinding.apply {
            tvDate.text = df.getDisplayDate(history.woHistoryWorkDate)
            tvWorkOrder.text = history.woHistoryWorkOrderId
            var display = ""
            if (history.woHistoryRegHours != 0.0) {
                display = "Reg: " +
                        nf.getNumberFromDouble(
                            history.woHistoryRegHours
                        )
            }
            if (history.woHistoryOtHours != 0.0) {
                if (display.isNotBlank()) {
                    display += " | "
                }
                display += "Ot: " +
                        nf.getNumberFromDouble(
                            history.woHistoryOtHours
                        )
            }
            if (history.woHistoryDblOtHours != 0.0) {
                if (display.isNotBlank()) {
                    display += " | "
                }
                display += "Ot: " +
                        nf.getNumberFromDouble(
                            history.woHistoryDblOtHours
                        )
            }
            tvDetails.text = display
            holder.itemView.setOnClickListener {
                chooseOptions(history)
            }
        }
    }

    private fun chooseOptions(history: WorkOrderHistory) {
        TODO("Set up the options for the work order history ")
    }

    override fun getItemCount(): Int {
        return workOrderHistory.size
    }
}