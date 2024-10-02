package ms.mattschlenkrich.paydaycalculator.adapter.workorders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paydaycalculator.databinding.ListWorkOrderHistoryDetailItemBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class WorkOrderHistoryAdapter(
    val mainActivity: MainActivity,
    val mView: View,
    private val workOrderHistory: List<WorkOrderHistoryWithDates>
) : RecyclerView.Adapter<WorkOrderHistoryAdapter.ViewHolder>() {

    private val df = DateFunctions()
    private val nf = NumberFunctions()

    class ViewHolder(
        val itemBinding: ListWorkOrderHistoryDetailItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListWorkOrderHistoryDetailItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val history = workOrderHistory[position]
        holder.itemBinding.apply {
            tvDate.text =
                df.getDisplayDate(history.workDate.wdDate)
            var display = ""
            val regHours = history.history.woHistoryRegHours
            if (regHours != 0.0) {
                display = "Reg: " +
                        nf.getNumberFromDouble(
                            regHours
                        )
            }
            if (history.history.woHistoryOtHours != 0.0) {
                if (display.isNotBlank()) {
                    display += " | "
                }
                display += "Ot: " +
                        nf.getNumberFromDouble(
                            history.history.woHistoryOtHours
                        )
            }
            if (history.history.woHistoryDblOtHours != 0.0) {
                if (display.isNotBlank()) {
                    display += " | "
                }
                display += "Ot: " +
                        nf.getNumberFromDouble(
                            history.history.woHistoryDblOtHours
                        )
            }
            tvHours.text = display
            tvSummary.text =
                history.history.woHistoryNote
        }
    }

    override fun getItemCount(): Int {
        return workOrderHistory.size
    }
}