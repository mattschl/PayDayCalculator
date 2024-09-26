package ms.mattschlenkrich.paydaycalculator.adapter.workorders

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrderJobSpecCombined
import ms.mattschlenkrich.paydaycalculator.databinding.ListSingleItemBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import ms.mattschlenkrich.paydaycalculator.ui.workOrder.WorkOrderUpdateFragmentDirections

class WorkOrderJobSpecAdapter(
    val mainActivity: MainActivity,
    val mView: View
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
                return oldItem.WorkOrderJobSpec.workOrderJobSpecId ==
                        newItem.WorkOrderJobSpec.workOrderJobSpecId &&
                        oldItem.jobSpec.jobSpecId ==
                        newItem.jobSpec.jobSpecId &&
                        oldItem.jobSpec.jsName ==
                        newItem.jobSpec.jsName
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
        val woJobSpec = differ.currentList[position]
        holder.itemBinding.apply {
            tvDisplay.text = woJobSpec.jobSpec.jsName
        }
        holder.itemView.setOnClickListener {
            chooseOptionsForJobSpec(woJobSpec)
        }
    }

    private fun chooseOptionsForJobSpec(woJobSpec: WorkOrderJobSpecCombined) {
        AlertDialog.Builder(mView.context)
            .setTitle("Choose option for ${woJobSpec.jobSpec.jsName}")
            .setPositiveButton("Edit description") { _, _ ->
                editJobSpec(woJobSpec)
            }
            .setNegativeButton("Remove") { _, _ ->
                removeJobSpec(woJobSpec)
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun removeJobSpec(woJobSpec: WorkOrderJobSpecCombined) {
        mainActivity.workOrderViewModel.deleteWorkOrderJobSpec(
            woJobSpec.WorkOrderJobSpec.workOrderJobSpecId,
            df.getCurrentTimeAsString()
        )
    }

    private fun editJobSpec(woJobSpec: WorkOrderJobSpecCombined) {
        mainActivity.mainViewModel.setJobSpec(woJobSpec.jobSpec)
        mView.findNavController().navigate(
            WorkOrderUpdateFragmentDirections
                .actionWorkOrderUpdateFragmentToJobSpecUpdateFragment()
        )
    }

}