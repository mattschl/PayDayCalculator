package ms.mattschlenkrich.paycalculator.ui.tax.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.database.model.tax.WorkTaxRules
import ms.mattschlenkrich.paycalculator.databinding.ListTaxRuleItemBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.tax.TaxRulesFragment

class TaxRuleAdapter(
    private val mainActivity: MainActivity,
    private val parentFragment: TaxRulesFragment,
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
            var display = mView.context.getString(R.string.level) + " " +
                    taxRule.wtLevel
            tvTaxLevel.text = display
            display = cf.getPercentStringFromDouble(taxRule.wtPercent)
            tvPercent.text = display
            if (taxRule.wtHasExemption) {
                tvExemption.visibility = View.VISIBLE
                display = mView.context.getString(R.string.exemption_) + " " +
                        cf.displayDollarsWithoutZeros(taxRule.wtExemptionAmount)
                tvExemption.text = display
            } else {
                tvExemption.visibility = View.GONE
            }
            if (taxRule.wtHasBracket) {
                tvLimit.visibility = View.VISIBLE
                display = mView.context.getString(R.string.upper_limit_) + " " +
                        cf.displayDollarsWithoutZeros(taxRule.wtBracketAmount)
                tvLimit.text = display
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
        parentFragment.gotoTaxRuleUpdateFragment()
    }

}