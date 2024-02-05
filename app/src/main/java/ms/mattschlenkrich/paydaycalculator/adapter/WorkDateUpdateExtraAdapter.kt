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
import ms.mattschlenkrich.paydaycalculator.model.WorkDateAndExtraDefAndWodDateExtras
import ms.mattschlenkrich.paydaycalculator.model.WorkDateExtras
import ms.mattschlenkrich.paydaycalculator.model.WorkDates
import ms.mattschlenkrich.paydaycalculator.ui.paydays.WorkDateUpdateFragment

class WorkDateUpdateExtraAdapter(
    val mainActivity: MainActivity,
    val mView: View,
    private val parentFragment: WorkDateUpdateFragment,
    private val workDate: WorkDates,
) : RecyclerView.Adapter<WorkDateUpdateExtraAdapter.ViewHolder>() {

    private val df = DateFunctions()
    private val cf = CommonFunctions()

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
        holder.itemBinding.apply {
            chkExtra.setOnClickListener {
                if (extra.workExtra == null) {
                    if (chkExtra.isChecked) {
                        addNewExtra(extra)
                    }
                } else {
                    updateExtra(extra, chkExtra.isChecked)
                }
            }
            var display = ""
            if (extra.workExtra == null) {
                display = extra.extraDef!!.extraType.wetName
                chkExtra.isChecked = extra.extraDef!!.extraType.wetIsDefault
            } else {
                display = extra.workExtra!!.wdeName
                chkExtra.isChecked = !extra.workExtra!!.wdeIsDeleted
            }
            chkExtra.text = display
        }
    }

    private fun updateExtra(
        extra: WorkDateAndExtraDefAndWodDateExtras, checked: Boolean
    ) {
        if (extra.workExtra != null) {
            parentFragment.let {
                mainActivity.payDayViewModel.updateWorkDateExtra(
                    WorkDateExtras(
                        extra.workExtra!!.workDateExtraId,
                        extra.workExtra!!.wdeWorkDateId,
                        extra.workExtra!!.wdeExtraTypeId,
                        extra.workExtra!!.wdeName,
                        extra.workExtra!!.wdeAppliesTo,
                        extra.workExtra!!.wdeAttachTo,
                        extra.workExtra!!.wdeValue,
                        extra.workExtra!!.wdeIsFixed,
                        extra.workExtra!!.wdeIsCredit,
                        !checked,
                        df.getCurrentTimeAsString()
                    )
                )
            }
        }
    }

    private fun addNewExtra(
        extra: WorkDateAndExtraDefAndWodDateExtras
    ) {
        if (extra.extraDef != null) {
            parentFragment.let {
                mainActivity.payDayViewModel.insertWorkDateExtra(
                    WorkDateExtras(
                        cf.generateId(),
                        workDate.workDateId,
                        extra.extraDef!!.extraType.workExtraTypeId,
                        extra.extraDef!!.extraType.wetName,
                        extra.extraDef!!.extraType.wetAppliesTo,
                        extra.extraDef!!.extraType.wetAttachTo,
                        extra.extraDef!!.definition.weValue,
                        extra.extraDef!!.definition.weIsFixed,
                        extra.extraDef!!.extraType.wetIsCredit,
                        false,
                        df.getCurrentTimeAsString()
                    )
                )
            }
        }
    }
}