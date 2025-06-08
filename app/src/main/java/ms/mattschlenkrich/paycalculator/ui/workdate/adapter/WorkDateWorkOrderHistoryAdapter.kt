package ms.mattschlenkrich.paycalculator.ui.workdate.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_500
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paycalculator.databinding.ListWorkOrderHistoryItemBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.workdate.WorkDateUpdateFragment

class WorkDateWorkOrderHistoryAdapter(
    private val workOrderHistory: ArrayList<WorkOrderHistoryWithDates>,
    val mainActivity: MainActivity,
    private val mView: View,
    private val workDateUpdateFragment: WorkDateUpdateFragment,
) : RecyclerView.Adapter<WorkDateWorkOrderHistoryAdapter.ViewHolder>() {

    //    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private val mainViewModel = mainActivity.mainViewModel
    private val workOrderViewModel = mainActivity.workOrderViewModel

    class ViewHolder(
        val itemBinding: ListWorkOrderHistoryItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListWorkOrderHistoryItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
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
                display = mView.context.getString(R.string.reg_) + nf.getNumberFromDouble(
                    history.history.woHistoryRegHours
                )
            }
            if (history.history.woHistoryOtHours != 0.0) {
                if (display.isNotBlank()) {
                    display += mView.context.getString(R.string.pipe)
                }
                display += mView.context.getString(R.string.ot_) + nf.getNumberFromDouble(
                    history.history.woHistoryOtHours
                )
            }
            if (history.history.woHistoryDblOtHours != 0.0) {
                if (display.isNotBlank()) {
                    display += mView.context.getString(R.string.pipe)
                }
                display += mView.context.getString(R.string.dbl_ot_) + nf.getNumberFromDouble(
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
        AlertDialog.Builder(mView.context).setTitle(
            mView.context.getString(R.string.choose_option_for_wo) + history.workOrder.woNumber
        ).setPositiveButton(mView.context.getString(R.string.edit)) { _, _ ->
            gotoWorkOrderHistoryUpdate(history)
        }.setNegativeButton(mView.context.getString(R.string.delete)) { _, _ ->
            confirmDeleteWorkOrderHistory(history)
        }.setNeutralButton(mView.context.getString(R.string.cancel), null).show()
    }

    private fun confirmDeleteWorkOrderHistory(history: WorkOrderHistoryWithDates) {
        AlertDialog.Builder(mView.context).setTitle(
            mView.context.getString(R.string.are_you_sure_you_want_to_delete_wo) + history.workOrder.woNumber
        ).setMessage(mView.context.getString(R.string.this_cannot_be_undone))
            .setPositiveButton(mView.context.getString(R.string.delete)) { _, _ ->
                deleteWorkOrderHistory(history.history.woHistoryId)
            }.setNeutralButton(mView.context.getString(R.string.cancel), null).show()
    }

    private fun deleteWorkOrderHistory(historyId: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            workOrderViewModel.removeAllWorkPerformedFromWorkOderHistory(
                historyId
            )
            workOrderViewModel.removeAllMaterialsFromWorkOrderHistory(
                historyId
            )
            delay(WAIT_500)
            workOrderViewModel.deleteWorkOrderHistory(historyId)
        }
    }

    override fun getItemCount(): Int {
        return workOrderHistory.size
    }

    private fun gotoWorkOrderHistoryUpdate(history: WorkOrderHistoryWithDates) {
        mainViewModel.setWorkOrderHistory(history.history)
        workDateUpdateFragment.gotoWorkOrderHistoryUpdateFragment()
    }

}