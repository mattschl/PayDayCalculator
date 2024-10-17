package ms.mattschlenkrich.paydaycalculator.ui.workorder.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkPerformedInSequence
import ms.mattschlenkrich.paydaycalculator.databinding.ListSingleItemBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import ms.mattschlenkrich.paydaycalculator.ui.workorder.WorkOrderHistoryUpdateFragmentDirections

class WorKOrderHistoryWorkPerformedAdapter(
    val mainActivity: MainActivity,
    val mView: View,
) : RecyclerView.Adapter<WorKOrderHistoryWorkPerformedAdapter.ViewHolder>() {

    private val df = DateFunctions()

    class ViewHolder(
        val itemBinding: ListSingleItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<WorkPerformedInSequence>() {
            override fun areItemsTheSame(
                oldItem: WorkPerformedInSequence,
                newItem: WorkPerformedInSequence
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: WorkPerformedInSequence,
                newItem: WorkPerformedInSequence
            ): Boolean {
                return oldItem.workPerformedHistoryId ==
                        newItem.workPerformedHistoryId &&
                        oldItem.wpWorkPerformedId ==
                        newItem.wpWorkPerformedId &&
                        oldItem.wpDescription ==
                        newItem.wpDescription &&
                        oldItem.wpSequence ==
                        newItem.wpSequence
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
        val woWorkPerformed = differ.currentList[position]
        holder.itemBinding.apply {
            val display = "${woWorkPerformed.wpSequence}) " +
                    woWorkPerformed.wpDescription
            tvDisplay.text = display
        }
        holder.itemView.setOnClickListener {
            chooseOptions(woWorkPerformed)
        }
    }

    private fun chooseOptions(woWorkPerformed: WorkPerformedInSequence) {
        AlertDialog.Builder(mView.context)
            .setTitle("Choose option for ${woWorkPerformed.wpDescription}")
            .setPositiveButton("Edit description") { _, _ ->
                editWorkPerformed(woWorkPerformed.wpWorkPerformedId)
            }
            .setNegativeButton("Remove") { _, _ ->
                removeWorkPerformedFromWorkOrder(
                    woWorkPerformed.workPerformedHistoryId
                )
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun editWorkPerformed(workPerformedId: Long) {
        mainActivity.workOrderViewModel.getWorkPerformed(
            workPerformedId
        ).observe(mView.findViewTreeLifecycleOwner()!!) { workPerformed ->
            mainActivity.mainViewModel.setWorkPerformed(workPerformed)
            mView.findNavController().navigate(
                WorkOrderHistoryUpdateFragmentDirections
                    .actionWorkOrderHistoryUpdateFragmentToWorkPerformedUpdateFragment()
            )
        }
    }

    private fun removeWorkPerformedFromWorkOrder(workOrderHistoryWorkPerformedId: Long) {
        mainActivity.workOrderViewModel.removeWorkPerformedFromWorkOderHistory(
            workOrderHistoryWorkPerformedId
        )
    }


}