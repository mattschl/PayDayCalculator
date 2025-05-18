package ms.mattschlenkrich.paycalculator.ui.workorder.workorder.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paycalculator.databinding.ListWorkOrderHistoryDetailItemBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.workorder.workorder.IWorkOrderUpdateFragment

class WorkOrderHistoryAdapter(
    val mainActivity: MainActivity,
    val mView: View,
    private val parentFragment: IWorkOrderUpdateFragment,
) : RecyclerView.Adapter<WorkOrderHistoryAdapter.ViewHolder>() {

    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private val mainViewModel = mainActivity.mainViewModel

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

    private val differCallBack =
        object : DiffUtil.ItemCallback<WorkOrderHistoryWithDates>() {
            override fun areItemsTheSame(
                oldItem: WorkOrderHistoryWithDates,
                newItem: WorkOrderHistoryWithDates
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: WorkOrderHistoryWithDates,
                newItem: WorkOrderHistoryWithDates
            ): Boolean {
                return oldItem.history.woHistoryId == newItem.history.woHistoryId &&
                        oldItem.workDate.workDateId == newItem.workDate.workDateId &&
                        oldItem.workOrder.workOrderId == newItem.workOrder.workOrderId
            }

        }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val history = differ.currentList[position]
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
            chooseOptions(history)
        }
    }

    private fun chooseOptions(history: WorkOrderHistoryWithDates) {
        AlertDialog.Builder(mView.context)
            .setTitle(
                mView.context.getString(R.string.choose_option_for) +
                        " ${mView.context.getString(R.string.work_performed_on)}" +
                        " ${df.getDisplayDate(history.workDate.wdDate)}"
            )
            .setPositiveButton(mView.context.getString(R.string.edit)) { _, _ ->
                gotoEditWorkOrderHistory(
                    history
                )
            }
            .setNegativeButton(mView.context.getString(R.string.cancel), null)
            .show()
    }

    private fun gotoEditWorkOrderHistory(history: WorkOrderHistoryWithDates) {
        mainViewModel.apply {
            setWorkOrderHistory(history.history)
            setWorkDateObject(history.workDate)
        }
        parentFragment.gotoWorkOrderHistoryFragment()
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}