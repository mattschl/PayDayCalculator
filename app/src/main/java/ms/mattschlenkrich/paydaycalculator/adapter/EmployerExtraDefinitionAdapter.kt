package ms.mattschlenkrich.paydaycalculator.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.CommonFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.ListEmployerExtraDefinitonBinding
import ms.mattschlenkrich.paydaycalculator.model.ExtraDefinitionFull

class EmployerExtraDefinitionAdapter(
    private val mainActivity: MainActivity,
    private val mView: View
) : RecyclerView.Adapter<EmployerExtraDefinitionAdapter.DefinitionViewHolder>() {

    private val cf = CommonFunctions()

    class DefinitionViewHolder(val itemBinding: ListEmployerExtraDefinitonBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<ExtraDefinitionFull>() {
            override fun areItemsTheSame(
                oldItem: ExtraDefinitionFull,
                newItem: ExtraDefinitionFull
            ): Boolean {
                return oldItem.employer.employerId == newItem.employer.employerId &&
                        oldItem.definition.workExtraId == newItem.definition.workExtraId
            }

            override fun areContentsTheSame(
                oldItem: ExtraDefinitionFull,
                newItem: ExtraDefinitionFull
            ): Boolean {
                return oldItem == newItem
            }
        }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefinitionViewHolder {
        return DefinitionViewHolder(
            ListEmployerExtraDefinitonBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: DefinitionViewHolder, position: Int) {
        val definition = differ.currentList[position]
        holder.itemBinding.tvName.text = definition.definition.weName
        var display = if (definition.definition.weIsCredit) {
            "Add "
        } else {
            "Deduct "
        }
        if (definition.definition.weIsCredit) {
            holder.itemBinding.tvValue.setTextColor(Color.BLACK)
        } else {
            holder.itemBinding.tvValue.setTextColor(Color.RED)
        }
        display += if (definition.definition.weIsFixed) {
            cf.displayDollars(
                definition.definition.weValue
            )
        } else {
            cf.displayPercentFromDouble(
                definition.definition.weValue / 100
            )
        }
        holder.itemBinding.tvValue.text = display
        display = "Effective starting " + definition.definition.weEffectiveDate
        holder.itemBinding.tvEffectiveDate.text = display
        val frequencies = mView.resources.getStringArray(
            R.array.extra_frequencies
        )
        display = "Calculated " + frequencies[definition.definition.weAppliesTo]
        holder.itemBinding.tvAppliesTo.text = display
        display = "Attaches to " + frequencies[definition.definition.weAttachTo]
        holder.itemBinding.tvAttachTo.text = display
        display = if (definition.definition.weIsDefault) {
            "This is the default"
        } else {
            "This needs to be manually added"
        }
        holder.itemBinding.tvIsDefault.text = display
    }
}