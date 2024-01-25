package ms.mattschlenkrich.paydaycalculator.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.databinding.ListWorkDateExtraItemBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkExtrasDefinitions

class WorkDateExtraAdapter(
) : RecyclerView.Adapter<WorkDateExtraAdapter.ExtraViewHolder>() {

    class ExtraViewHolder(
        val itemBinding: ListWorkDateExtraItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<WorkExtrasDefinitions>() {
            override fun areItemsTheSame(
                oldItem: WorkExtrasDefinitions,
                newItem: WorkExtrasDefinitions
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: WorkExtrasDefinitions,
                newItem: WorkExtrasDefinitions
            ): Boolean {
                return oldItem.workExtraDefId == newItem.workExtraDefId &&
                        oldItem.weExtraTypeId == newItem.weExtraTypeId &&
                        oldItem.weEffectiveDate == newItem.weEffectiveDate
            }
        }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExtraViewHolder {
        return ExtraViewHolder(
            ListWorkDateExtraItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ExtraViewHolder, position: Int) {
        val extra = differ.currentList[position]
//        holder.itemBinding.chkExtra.text = extra.weDefNameId
        holder.itemBinding.btnEdit.setOnClickListener {
            gotoExtraEdit(extra)
        }

    }

    private fun gotoExtraEdit(extra: WorkExtrasDefinitions) {
        //need to create a new fragment to edit the extra
    }
}