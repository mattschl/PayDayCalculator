package ms.mattschlenkrich.paydaycalculator.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.common.CommonFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.ListWorkDateExtraTimeSheetBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkDateExtras

class TimeSheetWorkDateExtraAdapter() :
    RecyclerView.Adapter<TimeSheetWorkDateExtraAdapter.DateExtrasHolder>() {

    private val cf = CommonFunctions()

    class DateExtrasHolder(
        val itemBinding: ListWorkDateExtraTimeSheetBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<WorkDateExtras>() {
            override fun areItemsTheSame(
                oldItem: WorkDateExtras,
                newItem: WorkDateExtras
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: WorkDateExtras,
                newItem: WorkDateExtras
            ): Boolean {
                return oldItem.workDateExtraId == newItem.workDateExtraId &&
                        oldItem.wdeName == newItem.wdeName
            }
        }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateExtrasHolder {
        return DateExtrasHolder(
            ListWorkDateExtraTimeSheetBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: DateExtrasHolder, position: Int) {
        val extra = differ.currentList[position]
        holder.itemBinding.apply {
            val display = extra.wdeName + " - " +
                    cf.displayDollars(extra.wdeValue)
            tvExtra.text = display
            if (extra.wdeIsCredit) {
                tvExtra.setTextColor(Color.BLACK)
            } else {
                tvExtra.setTextColor(Color.RED)
            }
        }
    }
}