package ms.mattschlenkrich.paycalculator.ui.workorder.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paycalculator.databinding.ListWorkOrderHistoryItemBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.timesheet.workdate.WorkDateUpdateFragment

class WorkDateWorkOrderHistoryAdapter(
    private val mainActivity: MainActivity,
    private val mView: View,
    private val workDateUpdateFragment: WorkDateUpdateFragment,
    private val workOrderHistory: ArrayList<WorkOrderHistoryWithDates>
) : RecyclerView.Adapter<WorkDateWorkOrderHistoryAdapter.ViewHolder>() {

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
            tvAddress.text = history.workOrder.woAddress
            tvWorkOrder.text = history.workOrder.woNumber
            var display = ""
            if (history.history.woHistoryRegHours != 0.0) {
                display = mView.context.getString(R.string.reg_) +
                        nf.getNumberFromDouble(
                            history.history.woHistoryRegHours
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
            tvDetails.text = display
            holder.itemView.setOnClickListener {
                chooseOptions(history)
            }
        }
    }

    private fun chooseOptions(history: WorkOrderHistoryWithDates) {
        AlertDialog.Builder(mView.context)
            .setTitle(
                mView.context.getString(R.string.choose_option_for_wo) +
                        history.workOrder.woNumber
            )
            .setPositiveButton(mView.context.getString(R.string.edit)) { _, _ ->
                gotoWorkOrderHistoryUpdate(history)
            }
            .setNegativeButton(mView.context.getString(R.string.delete)) { _, _ ->
                confirmDeleteWorkOrderHistory(history)
            }
            .setNeutralButton(mView.context.getString(R.string.cancel), null)
            .show()
    }

    private fun confirmDeleteWorkOrderHistory(history: WorkOrderHistoryWithDates) {
        AlertDialog.Builder(mView.context)
            .setTitle(
                mView.context.getString(R.string.are_you_sure_you_want_to_delete_wo) +
                        history.workOrder.woNumber
            )
            .setMessage(mView.context.getString(R.string.this_cannot_be_undone))
            .setPositiveButton(mView.context.getString(R.string.delete)) { _, _ ->
                mainActivity.workOrderViewModel.deleteWorkOrderHistory(
                    history.history.woHistoryId, df.getCurrentTimeAsString()
                )
            }
            .setNeutralButton(mView.context.getString(R.string.cancel), null)
            .show()
    }

    override fun getItemCount(): Int {
        return workOrderHistory.size
    }

    private fun gotoWorkOrderHistoryUpdate(history: WorkOrderHistoryWithDates) {
        mainActivity.mainViewModel.setWorkOrderHistory(history.history)
        workDateUpdateFragment.gotoWorkOrderHistoryUpdateFragment()
    }

}