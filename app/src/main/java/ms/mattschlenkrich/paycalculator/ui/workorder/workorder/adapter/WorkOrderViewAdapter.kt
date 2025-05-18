package ms.mattschlenkrich.paycalculator.ui.workorder.workorder.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrder
import ms.mattschlenkrich.paycalculator.databinding.ListWorkOrderItemBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.workorder.workorder.WorkOrderViewFragment

class WorkOrderViewAdapter(
    val mainActivity: MainActivity,
    val mView: View,
    private val parentTag: String,
    private val workOrderViewFragment: WorkOrderViewFragment,
) : RecyclerView.Adapter<WorkOrderViewAdapter.ViewHolder>() {

    //    private val df = DateFunctions()
//    private val nf = NumberFunctions()
    private val mainViewModel = mainActivity.mainViewModel

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
            .setTitle(
                mView.context.getString(R.string.choose_option_for_wo) +
                        workOrder.woNumber
            )
            .setMessage(
                mView.context.getString(R.string.would_you_like_to_open_this_work_order_to_view_or_edit_it)
            )
            .setPositiveButton(mView.context.getString(R.string.open)) { _, _ ->
                setWorkOrder(workOrder)
                gotoWorkOrderUpdate()
            }
            .setNegativeButton(mView.context.getString(R.string.cancel), null)
            .show()
    }

    private fun setWorkOrder(workOrder: WorkOrder) {
        mainViewModel.apply {
            setWorkOrder(workOrder)
            setWorkOrderNumber(workOrder.woNumber)
        }
    }

    private fun gotoWorkOrderUpdate() {
        mainViewModel.setCallingFragment(parentTag)
        workOrderViewFragment.gotoWorkOrderUpdateFragment()
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}