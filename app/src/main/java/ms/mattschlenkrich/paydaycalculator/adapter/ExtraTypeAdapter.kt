package ms.mattschlenkrich.paydaycalculator.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.databinding.ListSingleItemBinding
import ms.mattschlenkrich.paydaycalculator.model.extras.WorkExtraTypes

class ExtraTypeAdapter
    : RecyclerView.Adapter<ExtraTypeAdapter.ExtraTypeViewHolder>() {


    class ExtraTypeViewHolder(val itemBinding: ListSingleItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<WorkExtraTypes>() {
            override fun areItemsTheSame(
                oldItem: WorkExtraTypes,
                newItem: WorkExtraTypes
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: WorkExtraTypes,
                newItem: WorkExtraTypes
            ): Boolean {
                return oldItem.workExtraTypeId == newItem.workExtraTypeId &&
                        oldItem.wetName == newItem.wetName
            }
        }
    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExtraTypeViewHolder {
        return ExtraTypeViewHolder(
            ListSingleItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ExtraTypeViewHolder, position: Int) {
        val extraType = differ.currentList[position]
        var display = extraType.wetName
        if (extraType.wetIsDeleted) {
            display += " *DELETED*"
            holder.itemBinding.tvDisplay.setTextColor(Color.RED)
        } else {
            holder.itemBinding.tvDisplay.setTextColor(Color.BLACK)
        }
        holder.itemBinding.tvDisplay.text = display
        holder.itemView.setOnLongClickListener {
            //set up to go to item
            false
        }
    }
}
