package ms.mattschlenkrich.paydaycalculator.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.databinding.ListWorkOrderItemBinding
import ms.mattschlenkrich.paydaycalculator.model.workOrder.WorkOrder
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import ms.mattschlenkrich.paydaycalculator.ui.workOrder.WorkOrdersFragmentDirections

class WorkOrdersAdapter(
    val mainActivity: MainActivity,
    val mView: View,
    private val workOrderList: List<WorkOrder>
) : RecyclerView.Adapter<WorkOrdersAdapter.ViewHolder>() {

//    private val df = DateFunctions()
//    private val nf = NumberFunctions()

    class ViewHolder(
        val itemBinding: ListWorkOrderItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListWorkOrderItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workOrder = workOrderList[position]
        holder.itemBinding.apply {
            tvWorkOrderNumber.text = workOrder.woNumber
            tvAddress.text = workOrder.woAddress
            tvDescription.text = workOrder.woDescription
        }
        holder.itemView.setOnClickListener {
            chooseOptions(workOrder)
        }
    }

    private fun chooseOptions(workOrder: WorkOrder) {
        AlertDialog.Builder(mView.context)
            .setTitle("Choose options for ${workOrder.woNumber}")
            .setMessage(
                "Would you like to open this Work Order to view or edit it?"
            )
            .setPositiveButton("Open") { _, _ ->
                mainActivity.mainViewModel.setWorkOrder(workOrder)
                gotoWorkOrderUpdate()
            }
            .setNegativeButton("Cancel", null)
    }

    private fun gotoWorkOrderUpdate() {
        mView.findNavController().navigate(
            WorkOrdersFragmentDirections
                .actionWorkOrdersFragmentToWorkOrderUpdateFragment()
        )
    }

    override fun getItemCount(): Int {
        return workOrderList.size
    }
}