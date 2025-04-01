package ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory.adpater

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryWorkPerformedCombined
import ms.mattschlenkrich.paycalculator.databinding.ListSingleItemBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory.WorkOrderHistoryUpdateFragment

class WorKOrderHistoryWorkPerformedAdapter(
    private val mainActivity: MainActivity,
    private val workOrderHistoryUpdateFragment: WorkOrderHistoryUpdateFragment,
    private val parentFragment: String,
    private val curHistory: WorkOrderHistory,
    private val mView: View,
) : RecyclerView.Adapter<WorKOrderHistoryWorkPerformedAdapter.ViewHolder>() {

//    private val df = DateFunctions()

    class ViewHolder(
        val itemBinding: ListSingleItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<WorkOrderHistoryWorkPerformedCombined>() {
            override fun areItemsTheSame(
                oldItem: WorkOrderHistoryWorkPerformedCombined,
                newItem: WorkOrderHistoryWorkPerformedCombined
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: WorkOrderHistoryWorkPerformedCombined,
                newItem: WorkOrderHistoryWorkPerformedCombined
            ): Boolean {
                return oldItem.workPerformed.workPerformedId == newItem.workPerformed.workPerformedId &&
                        oldItem.workOrderHistoryWorkPerformed.workOrderHistoryWorkPerformedId ==
                        newItem.workOrderHistoryWorkPerformed.workOrderHistoryWorkPerformedId &&
                        oldItem.area == newItem.area
            }
        }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListSingleItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val work = differ.currentList[position]
        holder.itemBinding.apply {
            var display = "${position + 1}) " +
                    work.workPerformed.wpDescription
            display += if (work.area == null) {
                ""
            } else {
                " in ${work.area.areaName} "
            }
            display += if (work.workOrderHistoryWorkPerformed.wowpNote.isNullOrBlank()) {
                ""
            } else {
                " - ${work.workOrderHistoryWorkPerformed.wowpNote}."
            }
            tvDisplay.text = display
            holder.itemView.setOnClickListener {
                chooseOptions(work, display)
            }
        }
    }

    private fun chooseOptions(work: WorkOrderHistoryWorkPerformedCombined, display: String) {
        AlertDialog.Builder(mView.context)
            .setTitle(
                mView.context.getString(R.string.choose_option_for) +
                        display
            )
            .setItems(
                arrayOf(
                    mView.context.getString(R.string.edit_the_work_performed_description_in_the_history),
                    mView.context.getString(R.string.remove_this_work_performed_description_in_the_history),
                    mView.context.getString(R.string.edit_work_description_of_) +
                            " \" ${work.workPerformed.wpDescription} \"",
                    if (work.area != null) {
                        mView.context.getString(R.string.edit_area_description_of_) +
                                " \" ${work.area.areaName} \""
                    } else {
                        ""
                    }
                )
            ) { _, pos ->
                when (pos) {
                    0 -> {
                        gotoWorkPerformedHistoryEdit(
                            work.workOrderHistoryWorkPerformed.workOrderHistoryWorkPerformedId
                        )
                    }

                    1 -> {
                        removeWorkPerformedFromWorkOrder(
                            work.workOrderHistoryWorkPerformed.workOrderHistoryWorkPerformedId
                        )
                    }

                    2 -> {
                        editWorkPerformed(work.workOrderHistoryWorkPerformed.wowpWorkPerformedId)
                    }

                    3 -> {
                        editArea(work.workOrderHistoryWorkPerformed.wowpAreaId!!)
                    }
                }
            }
            .setNegativeButton(mView.context.getString(R.string.cancel), null)
            .show()
    }

    private fun editArea(areaId: Long) {
        mainActivity.mainViewModel.setAreaId(areaId)
        workOrderHistoryUpdateFragment.gotoAreaUpdateFragment()
    }

    private fun gotoWorkPerformedHistoryEdit(workPerformedHistoryId: Long) {
        mainActivity.mainViewModel.setWorkOrderHistory(curHistory)
        mainActivity.mainViewModel.setWorkPerformedHistoryId(workPerformedHistoryId)
        mainActivity.mainViewModel.addCallingFragment(parentFragment)
        workOrderHistoryUpdateFragment.gotoWorkOrderHistoryWorkPerformedUpdateFragment()
    }

    private fun editWorkPerformed(workPerformedId: Long) {
        mainActivity.mainViewModel.setWorkOrderHistory(curHistory)
        mainActivity.mainViewModel.setWorkPerformedId(workPerformedId)
        mainActivity.mainViewModel.addCallingFragment(parentFragment)
        workOrderHistoryUpdateFragment.gotoWorkPerformedUpdateFragment()
    }

    private fun removeWorkPerformedFromWorkOrder(workOrderHistoryWorkPerformedId: Long) {
        mainActivity.workOrderViewModel.removeWorkPerformedFromWorkOderHistory(
            workOrderHistoryWorkPerformedId
        )
    }

}