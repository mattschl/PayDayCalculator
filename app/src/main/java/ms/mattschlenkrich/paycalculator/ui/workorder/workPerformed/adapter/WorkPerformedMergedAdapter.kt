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
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkPerformed
import ms.mattschlenkrich.paycalculator.databinding.ListSingleItemBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.workorder.workPerformed.WorkPerformedMergeFragment

class WorkPerformedMergedAdapter(
    private val mainActivity: MainActivity,
    private val mView: View,
    private val workPerformedMergeFragment: WorkPerformedMergeFragment
) : RecyclerView.Adapter<WorkPerformedMergedAdapter.WorkPerformedViewHolder>() {

    val mainViewmodel = mainActivity.mainViewModel

    class WorkPerformedViewHolder(val itemBinding: ListSingleItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack = object : DiffUtil.ItemCallback<WorkPerformed>() {
        override fun areItemsTheSame(oldItem: WorkPerformed, newItem: WorkPerformed): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: WorkPerformed, newItem: WorkPerformed
        ): Boolean {
            return oldItem.workPerformedId == newItem.workPerformedId && oldItem.wpDescription == newItem.wpDescription
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
        val workPerformed = differ.currentList[position]
        holder.itemBinding.apply {
            var display = workPerformed.wpDescription
            if (workPerformed.wpIsDeleted) {
                display += mView.context.getString(R.string._deleted_)
                tvDisplay.setTextColor(Color.RED)
            } else {
                tvDisplay.setTextColor(Color.BLACK)
            }
            tvDisplay.text = display
        }
        holder.itemView.setOnClickListener {
            chooseOptions(workPerformed)

        }
    }

    private fun chooseOptions(workPerformed: WorkPerformed) {
        AlertDialog.Builder(mView.context)
            .setTitle("Choose if this is a parent or child")
            .setPositiveButton("Parent") { _, _ ->
                workPerformedMergeFragment.chooseToMergeAsParent(workPerformed)
            }
            .setNegativeButton("Child") { _, _ ->
                workPerformedMergeFragment.chooseToMergeAsChild(workPerformed)
            }
            .show()
    }
}