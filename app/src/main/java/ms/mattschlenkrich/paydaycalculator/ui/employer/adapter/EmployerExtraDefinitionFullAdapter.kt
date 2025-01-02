package ms.mattschlenkrich.paydaycalculator.ui.employer.adapter

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.database.model.extras.ExtraDefTypeAndEmployer
import ms.mattschlenkrich.paydaycalculator.database.model.extras.WorkExtrasDefinitions
import ms.mattschlenkrich.paydaycalculator.databinding.ListEmployerExtraDefinitonBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
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

    private val cf = NumberFunctions()
    private val df = DateFunctions()

    class DefinitionViewHolder(val itemBinding: ListEmployerExtraDefinitonBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<ExtraDefTypeAndEmployer>() {
            override fun areItemsTheSame(
                oldItem: ExtraDefTypeAndEmployer,
                newItem: ExtraDefTypeAndEmployer
            ): Boolean {
                return oldItem.employer.employerId == newItem.employer.employerId &&
                        oldItem.definition.workExtraDefId == newItem.definition.workExtraDefId
            }

            override fun areContentsTheSame(
                oldItem: ExtraDefTypeAndEmployer,
                newItem: ExtraDefTypeAndEmployer
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
        holder.itemBinding.apply {
            var display = definition.definition.weEffectiveDate

            if (definition.definition.weIsDeleted) {
                tvEffectiveDate.setTextColor(Color.RED)
                display = "* $display" +
                        mView.context.getString(R.string._deleted_)
            } else if (position == 0) {
                display += mView.context.getString(R.string.__current)
                tvEffectiveDate.setTextColor(Color.BLACK)
            } else {
                tvEffectiveDate.setTextColor(Color.BLACK)
            }
            tvEffectiveDate.text = display
            display = if (definition.extraType.wetIsCredit) {
                mView.context.getString(R.string.add)
            } else {
                mView.context.getString(R.string.deduct)
            }
            if (definition.extraType.wetIsCredit) {
                tvValue.setTextColor(Color.BLACK)
            } else {
                tvValue.setTextColor(Color.RED)
            }
            display += if (definition.definition.weIsFixed) {
                cf.displayDollars(
                    definition.definition.weValue
                )
            } else {
                cf.getPercentStringFromDouble(
                    definition.definition.weValue / 100
                )
            }
            tvValue.text = display
            if (definition.extraType.wetIsDefault) {
                tvInfo.text = mView.resources.getString(R.string._default)
                tvInfo.visibility = View.VISIBLE
            } else {
                tvInfo.visibility = View.GONE
            }
            holder.itemView.setOnClickListener {
                chooseOptionForDefinition(definition)
            }
        }
    }

    private fun chooseOptionForDefinition(definition: ExtraDefTypeAndEmployer) {
        AlertDialog.Builder(mView.context)
            .setTitle(
                mView.resources.getString(R.string.choose_an_action) +
                        mView.context.getString(R.string._for)
                        + definition.extraType.wetName
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
    }

    private fun deleteExtra(definition: WorkExtrasDefinitions) {
        mainActivity.workExtraViewModel.deleteWorkExtraDefinition(
            definition.workExtraDefId, df.getCurrentTimeAsString()
        )
        employerExtraDefinitionsFragment?.populateExtrasList()
        employerUpdateFragment?.populateExtras(definition.weEmployerId)
    }

    private fun gotoExtraUpdate(definition: ExtraDefTypeAndEmployer) {
        mainActivity.mainViewModel.setEmployerString(definition.employer.employerName)
        mainActivity.mainViewModel.setEmployer(definition.employer)
        mainActivity.mainViewModel.setExtraDefinitionFull(definition)
        if (employerExtraDefinitionsFragment != null) {
            gotoEmployerExtraDefinitionUpdateFragmentFromExtraDefinition()
        } else if (employerUpdateFragment != null) {
            gotoEmployerExtraDefinitionUpdateFragmentFromEmployerUpdate()
        }
    }

    private fun gotoEmployerExtraDefinitionUpdateFragmentFromExtraDefinition() {
        mView.findNavController().navigate(
            EmployerExtraDefinitionsFragmentDirections
                .actionEmployerExtraDefinitionsFragmentToEmployerExtraDefinitionUpdateFragment()
        )
    }

    private fun gotoEmployerExtraDefinitionUpdateFragmentFromEmployerUpdate() {
        mView.findNavController().navigate(
            EmployerUpdateFragmentDirections
                .actionEmployerUpdateFragmentToEmployerExtraDefinitionUpdateFragment()
        )
    }
}