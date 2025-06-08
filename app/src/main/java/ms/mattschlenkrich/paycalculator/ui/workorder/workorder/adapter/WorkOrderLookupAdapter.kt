package ms.mattschlenkrich.paycalculator.ui.workorder.workorder.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_ADD
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_UPDATE
import ms.mattschlenkrich.paycalculator.database.model.workorder.TempWorkOrderHistoryInfo
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrder
import ms.mattschlenkrich.paycalculator.databinding.ListWorkOrderItemBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.workorder.workorder.WorkOrderLookupFragment

class WorkOrderLookupAdapter(
    val mainActivity: MainActivity,
    val mView: View,
    private val workOrderLookupFragment: WorkOrderLookupFragment,
) : RecyclerView.Adapter<WorkOrderLookupAdapter.ViewHolder>() {

    //    private val df = DateFunctions()
//    private val nf = NumberFunctions()
    private val mainViewModel = mainActivity.mainViewModel

    class ViewHolder(
        val itemBinding: ListWorkOrderItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack = object : DiffUtil.ItemCallback<WorkOrder>() {
        override fun areItemsTheSame(oldItem: WorkOrder, newItem: WorkOrder): Boolean {
            return oldItem.workOrderId == newItem.workOrderId && oldItem.woNumber == newItem.woNumber
        }

        override fun areContentsTheSame(oldItem: WorkOrder, newItem: WorkOrder): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListWorkOrderItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
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
        holder.itemView.setOnClickListener { chooseOptions(workOrder) }
    }

    private fun chooseOptions(workOrder: WorkOrder) {
        AlertDialog.Builder(mView.context).setTitle(
            mView.context.getString(R.string.choose) + workOrder.woNumber
        ).setMessage(
            mView.context.getString(R.string.would_you_like_to_use_this_work_order) + mView.context.getString(
                R.string.line_break
            ) + workOrder.woDescription
        ).setPositiveButton(mView.context.getString(R.string.yes)) { _, _ ->
            chooseThisWorkOrder(workOrder)
        }.setNegativeButton(mView.context.getString(R.string.cancel), null).show()
    }

    private fun chooseThisWorkOrder(workOrder: WorkOrder) {
        setViewModelValues(workOrder)
        gotoCallingFragment()
    }

    private fun setViewModelValues(workOrder: WorkOrder) {
        mainViewModel.apply {
            setWorkOrder(workOrder)
            setWorkOrderNumber(workOrder.woNumber)
            if (getTempWorkOrderHistoryInfo() != null) {
                val history = getTempWorkOrderHistoryInfo()!!
                setTempWorkOrderHistoryInfo(
                    TempWorkOrderHistoryInfo(
                        workOrder.woNumber,
                        history.woHistoryWorkDate,
                        history.woHistoryRegHours,
                        history.woHistoryOtHours,
                        history.woHistoryDblOtHours,
                        history.woHistoryNote,
                        history.woWorkPerformed,
                        history.woArea,
                        history.woHistoryNote,
                        history.woMaterialQty,
                        history.woMaterial
                    )
                )
            }
        }
    }

    private fun gotoCallingFragment() {
        val callingFragment = mainViewModel.getCallingFragment()!!
        if (callingFragment.contains(FRAG_WORK_ORDER_HISTORY_UPDATE)) {
            gotoWorkOrderHistoryUpdate()
        }
        if (callingFragment.contains(FRAG_WORK_ORDER_HISTORY_ADD)) {
            gotoWorkOrderAdd()
        }
    }

    private fun gotoWorkOrderHistoryUpdate() {
        mainActivity.mainViewModel.setCallingFragment(
            mainActivity.mainViewModel.getCallingFragment()!!.replace(
                ", $FRAG_WORK_ORDER_HISTORY_UPDATE", ""
            )
        )
        workOrderLookupFragment.gotoWorkOrderHistoryUpdateFragment()
    }

    private fun gotoWorkOrderAdd() {
        mainActivity.mainViewModel.setCallingFragment(
            mainActivity.mainViewModel.getCallingFragment()!!.replace(
                ", $FRAG_WORK_ORDER_HISTORY_ADD", ""
            )
        )
        workOrderLookupFragment.gotoWorkOrderHistoryAddFragment()
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}