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
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderJobSpecCombined
import ms.mattschlenkrich.paycalculator.databinding.ListSingleItemBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.workorder.workorder.WorkOrderUpdateFragment

class WorkOrderJobSpecAdapter(
    private val mainActivity: MainActivity,
    private val workOrderUpdateFragment: WorkOrderUpdateFragment,
    private val mView: View
) : RecyclerView.Adapter<WorkOrderJobSpecAdapter.ViewHolder>() {

    private val df = DateFunctions()

    class ViewHolder(
        val itemBinding: ListSingleItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<WorkOrderJobSpecCombined>() {
            override fun areItemsTheSame(
                oldItem: WorkOrderJobSpecCombined,
                newItem: WorkOrderJobSpecCombined
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: WorkOrderJobSpecCombined,
                newItem: WorkOrderJobSpecCombined
            ): Boolean {
                return oldItem.workOrderJobSpec.workOrderJobSpecId ==
                        newItem.workOrderJobSpec.workOrderJobSpecId &&
                        oldItem.jobSpec.jobSpecId == newItem.jobSpec.jobSpecId &&
                        oldItem.jobSpec.jsName == newItem.jobSpec.jsName &&
                        oldItem.area?.areaId == newItem.area?.areaId &&
                        oldItem.area?.areaName == newItem.area?.areaName
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
        val jobSpec = differ.currentList[position]
        holder.itemBinding.apply {
            var display = jobSpec.jobSpec.jsName
            display +=
                if (jobSpec.workOrderJobSpec.wojsAreaId != null) {
                    mView.context.getString(R.string.pipe) + jobSpec.area?.areaName
                } else {
                    ""
                }
            display +=
                if (jobSpec.workOrderJobSpec.wojsNote != null) {
                    " - " + jobSpec.workOrderJobSpec.wojsNote
                } else {
                    ""
                }
            tvDisplay.text = display
        }
        holder.itemView.setOnClickListener {
            chooseOptionsForJobSpec(jobSpec)
        }
    }

    private fun chooseOptionsForJobSpec(jobSpec: WorkOrderJobSpecCombined) {
        AlertDialog.Builder(mView.context)
            .setTitle(
                mView.context.getString(R.string.choose_option_for) +
                        "\"${jobSpec.jobSpec.jsName}\""
            )
            .setItems(
                arrayOf(
                    mView.context.getString(R.string.edit_the_job_spec_description_in_the_work_order),
                    mView.context.getString(R.string.remove_this_work_job_spec_description_in_the_work_order),
                    mView.context.getString(R.string.edit_work_description_of) + jobSpec.jobSpec.jsName,
                    if (jobSpec.workOrderJobSpec.wojsAreaId != null) {
                        mView.context.getString(R.string.edit_area_description_of_) +
                                jobSpec.area?.areaName
                    } else {
                        ""
                    }
                )
            ) { _, pos ->
                when (pos) {
                    0 -> {
                        gotoJobSpecUpdate(
                            jobSpec.workOrderJobSpec.workOrderJobSpecId
                        )
                    }

                    1 -> {
                        removeJobSpecFromWorkOrder(
                            jobSpec
                        )
                    }

                    2 -> {
                        editJobSpec(jobSpec)
                    }

                    3 -> {
                        editArea(jobSpec.area!!.areaId)
                    }
                }
            }
            .setNegativeButton(mView.context.getString(R.string.cancel), null)

            .show()
    }

    private fun gotoJobSpecUpdate(workOrderJobSpecId: Long) {
        mainActivity.mainViewModel.setWorkOrderJobSpecId(workOrderJobSpecId)
    }

    private fun removeJobSpecFromWorkOrder(woJobSpec: WorkOrderJobSpecCombined) {
        mainActivity.workOrderViewModel.deleteWorkOrderJobSpec(
            woJobSpec.workOrderJobSpec.workOrderJobSpecId,
            df.getCurrentTimeAsString()
        )
    }

    private fun editJobSpec(woJobSpec: WorkOrderJobSpecCombined) {
        mainActivity.mainViewModel.setJobSpec(woJobSpec.jobSpec)
        workOrderUpdateFragment.gotoJobSpecUpdateFragment()
    }

    private fun editArea(areaId: Long) {
        mainActivity.mainViewModel.setAreaId(areaId)
    }

}