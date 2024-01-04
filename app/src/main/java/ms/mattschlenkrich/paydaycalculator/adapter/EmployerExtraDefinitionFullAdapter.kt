package ms.mattschlenkrich.paydaycalculator.adapter

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.CommonFunctions
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.ListEmployerExtraDefinitonBinding
import ms.mattschlenkrich.paydaycalculator.model.ExtraDefinitionFull
import ms.mattschlenkrich.paydaycalculator.model.WorkExtrasDefinitions
import ms.mattschlenkrich.paydaycalculator.ui.employer.EmployerUpdateFragment
import ms.mattschlenkrich.paydaycalculator.ui.employer.EmployerUpdateFragmentDirections
import ms.mattschlenkrich.paydaycalculator.ui.extras.EmployerExtraDefinitionsFragment
import ms.mattschlenkrich.paydaycalculator.ui.extras.EmployerExtraDefinitionsFragmentDirections

class EmployerExtraDefinitionFullAdapter(
    private val mainActivity: MainActivity,
    private val mView: View,
    private val employerExtraDefinitionsFragment: EmployerExtraDefinitionsFragment?,
    private val employerUpdateFragment: EmployerUpdateFragment?,
) : RecyclerView.Adapter<
        EmployerExtraDefinitionFullAdapter.DefinitionViewHolder>() {

    private val cf = CommonFunctions()
    private val df = DateFunctions()

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
        var display = definition.definition.weName
        if (definition.definition.weIsDeleted) {
            holder.itemBinding.tvName.setTextColor(Color.RED)
            display = "* $display * Deleted"
        } else {
            holder.itemBinding.tvName.setTextColor(Color.BLACK)
        }
        holder.itemBinding.tvName.text = display
        if (employerExtraDefinitionsFragment != null) {
            holder.itemBinding.tvName.textAlignment = View.TEXT_ALIGNMENT_CENTER
            display = if (definition.definition.weIsCredit) {
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
        } else {

            holder.itemBinding.tvName.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
            holder.itemBinding.tvValue.visibility = View.GONE
            holder.itemBinding.tvAppliesTo.visibility = View.GONE
            holder.itemBinding.tvAttachTo.visibility = View.GONE
            holder.itemBinding.tvIsDefault.visibility = View.GONE
            holder.itemBinding.tvEffectiveDate.visibility = View.GONE
        }
        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(mView.context)
                .setTitle(
                    mView.resources.getString(R.string.choose_an_action) +
                            " for " + definition.definition.weName
                )
                .setItems(
                    arrayOf(
                        mView.resources.getString(R.string.edit_this_item),
                        mView.resources.getString(R.string.delete_this_item),
                        mView.resources.getString(R.string.cancel)
                    )
                ) { _, pos ->
                    when (pos) {
                        0 -> {
                            gotoExtraUpdate(definition)
                        }

                        1 -> {
                            deleteExtra(definition.definition)
                        }

                        else -> {
                            //do nothing
                        }
                    }
                }.show()
            false
        }
    }

    private fun gotoExtraUpdate(definition: ExtraDefinitionFull) {
        mainActivity.mainViewModel.setEmployerString(definition.employer.employerName)
        mainActivity.mainViewModel.setEmployer(definition.employer)
        mainActivity.mainViewModel.setExtraDefinitionFull(definition)
        if (employerExtraDefinitionsFragment != null) {
            mView.findNavController().navigate(
                EmployerExtraDefinitionsFragmentDirections
                    .actionEmployerExtraDefinitionsFragmentToEmployerExtraDefinitionUpdateFragment()
            )
        } else if (employerUpdateFragment != null) {
            mView.findNavController().navigate(
                EmployerUpdateFragmentDirections
                    .actionEmployerUpdateFragmentToEmployerExtraDefinitionUpdateFragment()
            )
        }
    }

    private fun deleteExtra(definition: WorkExtrasDefinitions) {
        mainActivity.workExtraViewModel.deleteWorkExtraDefinition(
            definition.workExtraId, df.getCurrentTimeAsString()
        )
        employerExtraDefinitionsFragment?.fillExtrasList()
        employerUpdateFragment?.fillExtras(definition.weEmployerId)
    }
}