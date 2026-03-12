package ms.mattschlenkrich.paycalculator.ui.workorder.workPerformed.adapter

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.database.model.workorder.merged.WorkPerformedAndChild
import ms.mattschlenkrich.paycalculator.databinding.ListSingleItemBinding
import ms.mattschlenkrich.paycalculator.ui.workorder.workPerformed.WorkPerformedMergeFragment

class WorkPerformedChildrenAdapter(
    private val mView: View,
    private val workPerformedMergeFragment: WorkPerformedMergeFragment
) : RecyclerView.Adapter<WorkPerformedChildrenAdapter.WorkPerformedViewHolder>() {

    class WorkPerformedViewHolder(val itemBinding: ListSingleItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack = object : DiffUtil.ItemCallback<WorkPerformedAndChild>() {
        override fun areItemsTheSame(
            p0: WorkPerformedAndChild,
            p1: WorkPerformedAndChild
        ): Boolean {
            return p0 == p1
        }

        override fun areContentsTheSame(
            p0: WorkPerformedAndChild,
            p1: WorkPerformedAndChild
        ): Boolean {
            return p0.workPerformedMerged.workPerformedMergeId == p1.workPerformedMerged.workPerformedMergeId &&
                    p0.workPerformedParent.workPerformedId == p1.workPerformedParent.workPerformedId &&
                    p0.workPerformedChild.workPerformedId == p1.workPerformedChild.workPerformedId
        }
    }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkPerformedViewHolder {
        return WorkPerformedViewHolder(
            ListSingleItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: WorkPerformedViewHolder, position: Int) {
        val workPerformedAndChild = differ.currentList[position]
        holder.itemBinding.apply {
            var display = workPerformedAndChild.workPerformedChild.wpDescription
            if (workPerformedAndChild.workPerformedChild.wpIsDeleted) {
                display += mView.context.getString(R.string._deleted_)
                tvDisplay.setTextColor(Color.RED)
            } else {
                tvDisplay.setTextColor(Color.BLACK)
            }
            tvDisplay.text = display
        }
        holder.itemView.setOnClickListener {
            chooseOptions(workPerformedAndChild)

        }
    }

    private fun chooseOptions(workPerformedAndChild: WorkPerformedAndChild) {
        AlertDialog.Builder(mView.context)
            .setTitle("Remove child from master")
            .setPositiveButton("Remove") { _, _ ->
                workPerformedMergeFragment.removeWorkPerformedChild(workPerformedAndChild)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}