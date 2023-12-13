package ms.mattschlenkrich.paydaycalculator.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.databinding.ListSingleItemBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkExtraFrequencies
import ms.mattschlenkrich.paydaycalculator.ui.extras.ExtraFrequencyTypesFragmentDirections

class WorkExtraFrequencyAdapter(
    private val mainActivity: MainActivity,
    private val mView: View
) : RecyclerView.Adapter<WorkExtraFrequencyAdapter.ExtraFrequencyViewHolder>() {

    class ExtraFrequencyViewHolder(val itemBinding: ListSingleItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<WorkExtraFrequencies>() {
            override fun areContentsTheSame(
                oldItem: WorkExtraFrequencies,
                newItem: WorkExtraFrequencies
            ): Boolean {
                return oldItem.workExtraFrequencyId == newItem.workExtraFrequencyId &&
                        oldItem.workExtraFrequencyName == newItem.workExtraFrequencyName
            }

            override fun areItemsTheSame(
                oldItem: WorkExtraFrequencies,
                newItem: WorkExtraFrequencies
            ): Boolean {
                return oldItem == newItem
            }
        }
    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExtraFrequencyViewHolder {
        return ExtraFrequencyViewHolder(
            ListSingleItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ExtraFrequencyViewHolder, position: Int) {
        val extraFrequency = differ.currentList[position]
        var disp = extraFrequency.workExtraFrequencyName
        if (extraFrequency.wefIsDeleted) {
            disp += "*Deleted"
            holder.itemBinding.tvDisplay.setTextColor(Color.RED)
        } else {
            holder.itemBinding.tvDisplay.setTextColor(Color.BLACK)
        }
        holder.itemBinding.tvDisplay.text = disp
        holder.itemView.setOnClickListener {
            mainActivity.mainViewModel.setExtraFrequencyType(extraFrequency)
            mView.findNavController().navigate(
                ExtraFrequencyTypesFragmentDirections
                    .actionExtraFrequencyTypesFragmentToExtraFrequencyTypeUpdateFragment2()
            )
        }
    }
}