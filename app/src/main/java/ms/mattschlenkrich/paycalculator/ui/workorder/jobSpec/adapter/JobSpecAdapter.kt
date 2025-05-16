package ms.mattschlenkrich.paycalculator.ui.workorder.jobSpec.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.database.model.workorder.JobSpec
import ms.mattschlenkrich.paycalculator.databinding.ListSingleItemBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.workorder.jobSpec.JobSpecViewFragmentDirections

class JobSpecAdapter(
    private val mainActivity: MainActivity,
    private val mView: View,
    private val parentTag: String,
) : RecyclerView.Adapter<JobSpecAdapter.JobSpecViewHolder>() {

    private val mainViewModel = mainActivity.mainViewModel

    class JobSpecViewHolder(val itemBinding: ListSingleItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<JobSpec>() {
            override fun areItemsTheSame(oldItem: JobSpec, newItem: JobSpec): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: JobSpec, newItem: JobSpec): Boolean {
                return oldItem.jobSpecId == newItem.jobSpecId &&
                        oldItem.jsName == newItem.jsName
            }

        }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobSpecViewHolder {
        return JobSpecViewHolder(
            ListSingleItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: JobSpecViewHolder, position: Int) {
        val jobSpec = differ.currentList[position]
        holder.itemBinding.apply {
            var display = jobSpec.jsName
            if (jobSpec.jsIsDeleted) {
                display += mView.context.getString(R.string.deleted)
                tvDisplay.setTextColor(Color.RED)
            } else {
                tvDisplay.setTextColor(Color.BLACK)
            }
            tvDisplay.text = display
        }
        holder.itemView.setOnClickListener {
            gotoUpdateJobSpec(jobSpec)
        }
    }

    private fun gotoUpdateJobSpec(jobSpec: JobSpec?) {
        mainViewModel.apply {
            setCallingFragment(parentTag)
            setJobSpec(jobSpec)
        }
        gotoJobSpecUpdateFragment()
    }

    private fun gotoJobSpecUpdateFragment() {
        mView.findNavController().navigate(
            JobSpecViewFragmentDirections
                .actionJobSpecViewFragmentToJobSpecUpdateFragment()
        )
    }
}