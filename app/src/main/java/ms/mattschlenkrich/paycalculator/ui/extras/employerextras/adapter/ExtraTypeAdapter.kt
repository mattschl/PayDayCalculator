package ms.mattschlenkrich.paycalculator.ui.extras.employerextras.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.database.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.databinding.ListSingleItemBinding

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
        holder.itemBinding.apply {
            var display = extraType.wetName
            if (extraType.wetIsDeleted) {
                display += root.context.getString(R.string._deleted_)
                tvDisplay.setTextColor(Color.RED)
            } else {
                tvDisplay.setTextColor(Color.BLACK)
            }
            tvDisplay.text = display
            holder.itemView.setOnLongClickListener {
                //set up to go to item
                //TODO: add the functionality

                false
            }
        }
    }
}
