package ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory.adpater

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkPerformedInSequence
import ms.mattschlenkrich.paycalculator.databinding.ListSingleItemBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory.WorkOrderHistoryUpdateFragmentDirections

class WorKOrderHistoryWorkPerformedAdapter(
    val mainActivity: MainActivity,
    val parentFragment: String,
    val mView: View,
) : RecyclerView.Adapter<WorKOrderHistoryWorkPerformedAdapter.ViewHolder>() {

//    private val df = DateFunctions()

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
        val work = differ.currentList[position]
        holder.itemBinding.apply {
            var display = "${work.wpSequence}) " +
                    work.wpDescription
            display += if (work.wpArea.isBlank()) {
                ""
            } else {
                " in ${work.wpArea} "
            }
            display += if (work.wpNote.isNullOrBlank()) {
                ""
            } else {
                " - ${work.wpNote}."
            }
            tvDisplay.text = display
            holder.itemView.setOnClickListener {
                chooseOptions(work, display)
            }
        }
    }

    private fun chooseOptions(woWorkPerformed: WorkPerformedInSequence, display: String) {
        AlertDialog.Builder(mView.context)
            .setTitle(
                mView.context.getString(R.string.choose_option_for) +
                        display
            )
            .setItems(
                arrayOf(
                    "Edit the work performed description in the history",
                    "Remove this work performed description in the history",
                    "Edit Work description of ${woWorkPerformed.wpDescription}",
                    "Edit area description of ${woWorkPerformed.wpArea}"
                )
            ) { _, pos ->
                when (pos) {
                    0 -> {
                        gotoWorkPerformedHistoryEdit(woWorkPerformed.wpWorkPerformedId)
                    }

                    1 -> {
                        removeWorkPerformedFromWorkOrder(woWorkPerformed.workPerformedHistoryId)
                    }

                    2 -> {
                        editWorkPerformed(woWorkPerformed.wpWorkPerformedId)
                    }

                    3 -> {
                        editArea(woWorkPerformed.wpAreaId)
                    }
                }
            }
            .setNeutralButton(mView.context.getString(R.string.cancel), null)
            .show()
    }

    private fun editArea(areaId: Long) {

    }

    private fun gotoWorkPerformedHistoryEdit(wpWorkPerformedId: Long) {

    }

    private fun editWorkPerformed(workPerformedId: Long) {
        mainActivity.mainViewModel.setWorkPerformedId(workPerformedId)
        gotoWorkPerformedUpdateFragment()

        mainActivity.mainViewModel.addCallingFragment(parentFragment)
    }

    private fun removeWorkPerformedFromWorkOrder(workOrderHistoryWorkPerformedId: Long) {
        mainActivity.workOrderViewModel.removeWorkPerformedFromWorkOderHistory(
            workOrderHistoryWorkPerformedId
        )
    }

    private fun gotoWorkPerformedUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections
                .actionWorkOrderHistoryUpdateFragmentToWorkPerformedUpdateFragment()
        )
    }


}