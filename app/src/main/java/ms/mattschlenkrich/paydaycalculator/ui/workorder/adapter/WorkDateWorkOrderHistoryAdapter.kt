package ms.mattschlenkrich.paydaycalculator.ui.workorder.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paydaycalculator.databinding.ListWorkOrderHistoryItemBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import ms.mattschlenkrich.paydaycalculator.ui.paydays.WorkDateUpdateFragmentDirections

class WorkDateWorkOrderHistoryAdapter(
    val mainActivity: MainActivity,
    val mView: View,
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
                display = "Reg: " +
                        nf.getNumberFromDouble(
                            history.history.woHistoryRegHours
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
            tvDetails.text = display
            holder.itemView.setOnClickListener {
                chooseOptions(history)
            }
        }
    }

    private fun chooseOptions(history: WorkOrderHistoryWithDates) {
        AlertDialog.Builder(mView.context)
            .setTitle("Choose option for wo ${history.workOrder.woNumber}")
            .setPositiveButton("Edit") { _, _ ->
                editWorkOrderHistory(history)
            }
            .setNegativeButton("Delete") { _, _ ->
                confirmDeleteWorkOrderHistory(history)
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteWorkOrderHistory(history: WorkOrderHistoryWithDates) {
        AlertDialog.Builder(mView.context)
            .setTitle("Are you sure you want to delete wo ${history.workOrder.woNumber}")
            .setMessage("This cannot be undone!")
            .setPositiveButton("DELETE") { _, _ ->
                mainActivity.workOrderViewModel.deleteWorkOrderHistory(
                    history.history.woHistoryId, df.getCurrentTimeAsString()
                )
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun editWorkOrderHistory(history: WorkOrderHistoryWithDates) {
        mainActivity.mainViewModel.setWorkOrderHistory(history.history)
        mView.findNavController().navigate(
            WorkDateUpdateFragmentDirections
                .actionWorkDateUpdateFragmentToWorkOrderHistoryUpdateFragment()
        )

    }

    override fun getItemCount(): Int {
        return workOrderHistory.size
    }
}