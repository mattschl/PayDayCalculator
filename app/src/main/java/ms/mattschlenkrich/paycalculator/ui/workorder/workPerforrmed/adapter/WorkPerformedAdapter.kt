package ms.mattschlenkrich.paycalculator.ui.workorder.workPerforrmed.adapter

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
import ms.mattschlenkrich.paycalculator.ui.workorder.workPerforrmed.WorkPerformedViewFragment

class WorkPerformedAdapter(
    val mainActivity: MainActivity,
    private val mView: View,
    private val parentTag: String,
    private val workPerformedViewFragment: WorkPerformedViewFragment
) : RecyclerView.Adapter<WorkPerformedAdapter.WorkPerformedViewHolder>() {

    private val mainViewModel = mainActivity.mainViewModel

    class WorkPerformedViewHolder(val itemBinding: ListSingleItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<WorkPerformed>() {
            override fun areItemsTheSame(oldItem: WorkPerformed, newItem: WorkPerformed): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: WorkPerformed,
                newItem: WorkPerformed
            ): Boolean {
                return oldItem.workPerformedId == newItem.workPerformedId &&
                        oldItem.wpDescription == newItem.wpDescription
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
            gotoWorkPerformedUpdate(workPerformed.workPerformedId)
        }
    }

    private fun gotoWorkPerformedUpdate(workPerformedId: Long) {
        mainViewModel.apply {
            setCallingFragment(parentTag)
            setWorkPerformedId(workPerformedId)
        }
        workPerformedViewFragment.gotoWorkPerformedUpdateFragment()

    }
}