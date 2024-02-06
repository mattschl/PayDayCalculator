package ms.mattschlenkrich.paydaycalculator.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.common.CommonFunctions
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.ListWorkDateExtraItemBinding
import ms.mattschlenkrich.paydaycalculator.model.ExtraDefinitionAndType
import ms.mattschlenkrich.paydaycalculator.model.WorkDateExtras
import ms.mattschlenkrich.paydaycalculator.model.WorkDates
import ms.mattschlenkrich.paydaycalculator.ui.paydays.WorkDateUpdateFragment

class WorkDateUpdateExtraAdapter(
    val mainActivity: MainActivity,
    val mView: View,
    private val parentFragment: WorkDateUpdateFragment,
    private val workDate: WorkDates,
    private val workDateExtras: ArrayList<WorkDateExtras>
) : RecyclerView.Adapter<WorkDateUpdateExtraAdapter.ViewHolder>() {

    private val df = DateFunctions()
    private val cf = CommonFunctions()

    class ViewHolder(
        val itemBinding: ListWorkDateExtraItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil
        .ItemCallback<ExtraDefinitionAndType>() {
            override fun areItemsTheSame(
                oldItem: ExtraDefinitionAndType,
                newItem: ExtraDefinitionAndType
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: ExtraDefinitionAndType,
                newItem: ExtraDefinitionAndType
            ): Boolean {
                return oldItem.extraType == newItem.extraType &&
                        oldItem.definition == newItem.definition
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
        holder.itemBinding.apply {
            chkExtra.setOnClickListener {
                if (chkExtra.isChecked) {
                    addNewExtra(extra)
                } else {
                    deleteExtra(extra.extraType.wetName, workDate.workDateId)
                }
            }
            val display =
                "${extra.extraType.wetName} ${cf.displayDollars(extra.definition.weValue)}"
            chkExtra.text = display
            var found = false
            for (workExtra in workDateExtras) {
                if (workExtra.wdeName == extra.extraType.wetName) {
                    found = true
                }
            }
            chkExtra.isChecked = found
        }
    }

    private fun deleteExtra(extraName: String, workDateId: Long) {
        parentFragment.let {
            mainActivity.payDayViewModel.deleteWorkDateExtra(
                extraName, workDateId, df.getCurrentTimeAsString()
            )
        }
    }

    private fun addNewExtra(
        extra: ExtraDefinitionAndType
    ) {
        parentFragment.let {
            mainActivity.payDayViewModel.insertWorkDateExtra(
                WorkDateExtras(
                    cf.generateId(),
                    workDate.workDateId,
                    extra.extraType.workExtraTypeId,
                    extra.extraType.wetName,
                    extra.extraType.wetAppliesTo,
                    extra.extraType.wetAttachTo,
                    extra.definition.weValue,
                    extra.definition.weIsFixed,
                    extra.extraType.wetIsCredit,
                    false,
                    df.getCurrentTimeAsString()
                )
            )
        }
        parentFragment.fillExtras()
    }
}