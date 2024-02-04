package ms.mattschlenkrich.paydaycalculator.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.databinding.ListWorkDateExtraItemBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkDateAndExtraDefAndWodDateExtras
import ms.mattschlenkrich.paydaycalculator.model.WorkDates
import ms.mattschlenkrich.paydaycalculator.ui.paydays.WorkDateUpdateFragment

class WorkDateUpdateExtraAdapter(
    val mainActivity: MainActivity,
    val mView: View,
    private val parentFragment: WorkDateUpdateFragment,
) : RecyclerView.Adapter<WorkDateUpdateExtraAdapter.ViewHolder>() {

    private val workDate: WorkDates? = null

    class ViewHolder(
        val itemBinding: ListWorkDateExtraItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil
        .ItemCallback<WorkDateAndExtraDefAndWodDateExtras>() {
            override fun areItemsTheSame(
                oldItem: WorkDateAndExtraDefAndWodDateExtras,
                newItem: WorkDateAndExtraDefAndWodDateExtras
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: WorkDateAndExtraDefAndWodDateExtras,
                newItem: WorkDateAndExtraDefAndWodDateExtras
            ): Boolean {
                return oldItem.workDate.workDateId == newItem.workDate.workDateId &&
                        oldItem.workExtra == newItem.workExtra &&
                        oldItem.extraDef == newItem.extraDef
            }
        }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListWorkDateExtraItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val extra = differ.currentList[position]
        var display = if (extra.extraDef != null) {
            extra.extraDef!!.extraType.wetName
        } else {
            extra.workExtra!!.wdeName
        }
        holder.itemBinding.chkExtra.text = display
    }
}