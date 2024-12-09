package ms.mattschlenkrich.paydaycalculator.ui.workorder.workorder.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.common.FRAG_WORK_ORDER_HISTORY_ADD
import ms.mattschlenkrich.paydaycalculator.common.FRAG_WORK_ORDER_HISTORY_UPDATE
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.TempWorkOrderHistoryInfo
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkOrder
import ms.mattschlenkrich.paydaycalculator.databinding.ListWorkOrderItemBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import ms.mattschlenkrich.paydaycalculator.ui.workorder.workorder.WorkOrderLookupFragmentDirections

class WorkOrderLookupAdapter(
    val mainActivity: MainActivity,
    val mView: View
) : RecyclerView.Adapter<WorkOrderLookupAdapter.ViewHolder>() {

//    private val df = DateFunctions()
//    private val nf = NumberFunctions()

    class ViewHolder(
        val itemBinding: ListWorkOrderItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<WorkOrder>() {
            override fun areItemsTheSame(oldItem: WorkOrder, newItem: WorkOrder): Boolean {
                return oldItem.workOrderId == newItem.workOrderId &&
                        oldItem.woNumber == newItem.woNumber
            }

            override fun areContentsTheSame(oldItem: WorkOrder, newItem: WorkOrder): Boolean {
                return oldItem == newItem
            }
        }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListWorkOrderItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workOrder = differ.currentList[position]
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
            .setTitle("Choose ${workOrder.woNumber}?")
            .setMessage(
                "Would you like to use this Work Order?"
            )
            .setPositiveButton("Yes") { _, _ ->
                mainActivity.mainViewModel.setWorkOrder(workOrder)
                mainActivity.mainViewModel.setWorkOrderNumber(
                    workOrder.woNumber
                )
                gotoCallingFragment(workOrder)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun gotoCallingFragment(workOrder: WorkOrder) {
        setViewModelValues(workOrder)
        if (mainActivity.mainViewModel.getCallingFragment()!!.contains(
                FRAG_WORK_ORDER_HISTORY_UPDATE
            )
        ) {
            gotoWorkOrderHistoryUpdate()
        }
        if (mainActivity.mainViewModel.getCallingFragment()!!.contains(
                FRAG_WORK_ORDER_HISTORY_ADD
            )
        ) {
            gotoWorkOrderAddFragment()
        }
    }

    private fun setViewModelValues(workOrder: WorkOrder) {
        mainActivity.mainViewModel.setWorkOrder(workOrder)
        mainActivity.mainViewModel.setWorkOrderNumber(workOrder.woNumber)
        if (mainActivity.mainViewModel.getTempWorkOrderHistoryInfo() != null) {
            val history =
                mainActivity.mainViewModel.getTempWorkOrderHistoryInfo()!!
            mainActivity.mainViewModel.setTempWorkOrderHistoryInfo(
                TempWorkOrderHistoryInfo(
                    workOrder.woNumber,
                    history.woHistoryWorkDate,
                    history.woHistoryRegHours,
                    history.woHistoryOtHours,
                    history.woHistoryDblOtHours,
                    history.woHistoryNote,
                    history.woWorkPerformed,
                    history.woMaterialQty,
                    history.woMaterial
                )
            )
        }
    }

    private fun gotoWorkOrderHistoryUpdate() {
        mainActivity.mainViewModel.setCallingFragment(
            mainActivity.mainViewModel.getCallingFragment()!!.replace(
                ", $FRAG_WORK_ORDER_HISTORY_UPDATE", ""
            )
        )
        mView.findNavController().navigate(
            WorkOrderLookupFragmentDirections
                .actionWorkOrderLookupFragmentToWorkOrderHistoryUpdateFragment()
        )
    }

    private fun gotoWorkOrderAddFragment() {
        mainActivity.mainViewModel.setCallingFragment(
            mainActivity.mainViewModel.getCallingFragment()!!.replace(
                ", $FRAG_WORK_ORDER_HISTORY_ADD", ""
            )
        )
        mView.findNavController().navigate(
            WorkOrderLookupFragmentDirections
                .actionWorkOrderLookupFragmentToWorkOrderHistoryAddFragment()
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}