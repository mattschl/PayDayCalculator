package ms.mattschlenkrich.paydaycalculator.ui.workorder.workorderHistory.adpater

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paydaycalculator.databinding.ListWorkOrderHistoryDetailItemBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import ms.mattschlenkrich.paydaycalculator.ui.workorder.workorder.WorkOrderUpdateFragmentDirections

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
                display = mView.context.getString(R.string.reg_) +
                        nf.getNumberFromDouble(
                            regHours
                        )
            }
            if (history.history.woHistoryOtHours != 0.0) {
                if (display.isNotBlank()) {
                    display += mView.context.getString(R.string.pipe)
                }
                display += mView.context.getString(R.string.ot_) +
                        nf.getNumberFromDouble(
                            history.history.woHistoryOtHours
                        )
            }
            if (history.history.woHistoryDblOtHours != 0.0) {
                if (display.isNotBlank()) {
                    display += mView.context.getString(R.string.pipe)
                }
                display += mView.context.getString(R.string.dbl_ot_) +
                        nf.getNumberFromDouble(
                            history.history.woHistoryDblOtHours
                        )
            }
            tvHours.text = display
            if (history.history.woHistoryNote.isNullOrBlank()) {
                tvSummary.visibility = View.GONE
            } else {
                tvSummary.text =
                    history.history.woHistoryNote
                tvSummary.visibility = View.VISIBLE
            }
        }
        holder.itemView.setOnClickListener {
            gotoEditWorkOrderHistory(history)
        }
    }

    private fun gotoEditWorkOrderHistory(history: WorkOrderHistoryWithDates) {
        mainActivity.mainViewModel.setWorkOrderHistory(
            history.history
        )
        mainActivity.mainViewModel.setWorkDateObject(
            history.workDate
        )
        gotoWorkOrderHistoryFragment()
    }

    private fun gotoWorkOrderHistoryFragment() {
        mView.findNavController().navigate(
            WorkOrderUpdateFragmentDirections
                .actionWorkOrderUpdateFragmentToWorkOrderHistoryUpdateFragment()
        )
    }

    override fun getItemCount(): Int {
        return workOrderHistory.size
    }
}