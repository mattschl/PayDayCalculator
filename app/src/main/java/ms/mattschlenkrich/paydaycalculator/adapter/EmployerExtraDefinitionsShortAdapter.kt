package ms.mattschlenkrich.paydaycalculator.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.common.CommonFunctions
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.ListEmployerExtraDefinitonShortBinding
import ms.mattschlenkrich.paydaycalculator.model.ExtraDefinitionFull
import ms.mattschlenkrich.paydaycalculator.ui.employer.EmployerUpdateFragmentDirections

class EmployerExtraDefinitionsShortAdapter(
    private val mainActivity: MainActivity,
    private val mView: View,
) : RecyclerView.Adapter<EmployerExtraDefinitionsShortAdapter.DefinitionViewHolder>() {


    private val cf = CommonFunctions()
    private val df = DateFunctions()

    class DefinitionViewHolder(val itemBinding: ListEmployerExtraDefinitonShortBinding) :
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
            ListEmployerExtraDefinitonShortBinding.inflate(
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
        display = if (definition.definition.weIsFixed) {
            cf.displayDollars(definition.definition.weValue)
        } else {
            cf.displayPercentFromDouble(definition.definition.weValue)
        }
        if (definition.definition.weIsCredit) {
            display = "Add $display"
            holder.itemBinding.tvValue.setTextColor(Color.BLACK)
        } else {
            holder.itemBinding.tvValue.setTextColor(Color.RED)
            display = "Deduct $display"
        }
        holder.itemBinding.tvValue.text = display
        holder.itemBinding.btnEdit.setOnClickListener {
            gotoExtraUpdate(definition)
        }
    }

    private fun gotoExtraUpdate(definition: ExtraDefinitionFull) {
        mainActivity.mainViewModel.setEmployerString(definition.employer.employerName)
        mainActivity.mainViewModel.setEmployer(definition.employer)
        mainActivity.mainViewModel.setExtraDefinitionFull(definition)
        mView.findNavController().navigate(
            EmployerUpdateFragmentDirections
                .actionEmployerUpdateFragmentToEmployerExtraDefinitionUpdateFragment()
        )
    }
}