package ms.mattschlenkrich.paydaycalculator.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.ListTaxRuleItemBinding
import ms.mattschlenkrich.paydaycalculator.model.tax.WorkTaxRules
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import ms.mattschlenkrich.paydaycalculator.ui.tax.TaxRulesFragmentDirections

class TaxRuleAdapter(
    private val mainActivity: MainActivity,
    private val mView: View
) : RecyclerView.Adapter<TaxRuleAdapter.TaxRuleViewHolder>() {

    private val cf = NumberFunctions()

    class TaxRuleViewHolder(val itemBinding: ListTaxRuleItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<WorkTaxRules>() {
            override fun areContentsTheSame(
                oldItem: WorkTaxRules,
                newItem: WorkTaxRules
            ): Boolean {
                return oldItem.workTaxRuleId == newItem.workTaxRuleId &&
                        oldItem.wtType == newItem.wtType &&
                        oldItem.wtLevel == newItem.wtLevel &&
                        oldItem.wtEffectiveDate == newItem.wtEffectiveDate
            }

            override fun areItemsTheSame(
                oldItem: WorkTaxRules,
                newItem: WorkTaxRules
            ): Boolean {
                return oldItem == newItem
            }
        }
    val differ = AsyncListDiffer(this, differCallBack)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaxRuleViewHolder {
        return TaxRuleViewHolder(
            ListTaxRuleItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: TaxRuleViewHolder, position: Int) {
        val taxRule = differ.currentList[position]
        holder.itemBinding.apply {
            var disp = "Level " + taxRule.wtLevel
            tvTaxLevel.text = disp
            disp = cf.displayPercentFromDouble(taxRule.wtPercent)
            tvPercent.text = disp
            if (taxRule.wtHasExemption) {
                tvExemption.visibility = View.VISIBLE
                disp = "Exemption: " + cf.displayDollars(taxRule.wtExemptionAmount)
                tvExemption.text = disp
            } else {
                tvExemption.visibility = View.GONE
            }
            if (taxRule.wtHasBracket) {
                tvLimit.visibility = View.VISIBLE
                disp = "Upper Limit: " + cf.displayDollars(taxRule.wtBracketAmount)
                tvLimit.text = disp
            } else {
                tvLimit.visibility = View.GONE
            }
            holder.itemView.setOnClickListener {
                chooseOptions(taxRule)
            }
        }
    }

    private fun chooseOptions(taxRule: WorkTaxRules) {
        mainActivity.mainViewModel.setTaxRule(taxRule)
        mView.findNavController().navigate(
            TaxRulesFragmentDirections
                .actionTaxRulesFragmentToTaxRuleUpdateFragment()
        )
    }
}