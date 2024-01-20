package ms.mattschlenkrich.paydaycalculator.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.databinding.ListWorkDateExtraItemBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkDatesExtras

class WorkDateExtraAdapter(
    private val mainActivity: MainActivity,
    private val mView: View,
) : RecyclerView.Adapter<WorkDateExtraAdapter.ExtraViewHolder>() {

    class ExtraViewHolder(
        val itemBinding: ListWorkDateExtraItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<WorkDatesExtras>() {
            override fun areItemsTheSame(
                oldItem: WorkDatesExtras,
                newItem: WorkDatesExtras
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: WorkDatesExtras,
                newItem: WorkDatesExtras
            ): Boolean {
                return oldItem.wdeId == newItem.wdeId &&
                        oldItem.wdeEmployerId == newItem.wdeEmployerId &&
                        oldItem.wdeName == newItem.wdeName
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
        holder.itemBinding.chkExtra.text = extra.wdeName
        holder.itemBinding.btnEdit.setOnClickListener {
            gotoExtraEdit(extra)
        }

    }

    private fun gotoExtraEdit(extra: WorkDatesExtras) {
        //need to create a new fragment to edit the extra
    }
}