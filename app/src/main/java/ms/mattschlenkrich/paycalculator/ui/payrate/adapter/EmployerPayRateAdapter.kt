package ms.mattschlenkrich.paycalculator.ui.payrate.adapter

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.database.model.employer.EmployerPayRates
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.databinding.ListWagesItemBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.payrate.EmployerPayRatesFragment

class EmployerPayRateAdapter(
    val mainActivity: MainActivity,
    private val mView: View,
    private val curEmployer: Employers,
    private val parentTag: String,
    private val employerPayRatesFragment: EmployerPayRatesFragment,
) : RecyclerView.Adapter<EmployerPayRateAdapter.WageViewHolder>() {

    private val cf = NumberFunctions()
    private val mainViewModel = mainActivity.mainViewModel

    private inline var TextView.strike: Boolean
        set(visible) {
            paintFlags = if (visible) paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            else paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        get() = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG == Paint.STRIKE_THRU_TEXT_FLAG

    class WageViewHolder(val itemBinding: ListWagesItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack = object : DiffUtil.ItemCallback<EmployerPayRates>() {
        override fun areItemsTheSame(
            oldItem: EmployerPayRates, newItem: EmployerPayRates
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: EmployerPayRates, newItem: EmployerPayRates
        ): Boolean {
            return oldItem.employerPayRateId == newItem.employerPayRateId && oldItem.eprEmployerId == newItem.eprEmployerId && oldItem.eprPayRate == newItem.eprPayRate && oldItem.eprPerPeriod == newItem.eprPerPeriod
        }
    }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WageViewHolder {
        return WageViewHolder(
            ListWagesItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: WageViewHolder, position: Int) {
        val wage = differ.currentList[position]
        holder.itemBinding.apply {
            if (wage.eprIsDeleted) {
                tvEffectiveDate.strike = true
                tvEffectiveDate.setTextColor(Color.RED)
                tvWage.strike = true
                tvWage.setTextColor(Color.RED)
            } else {
                tvEffectiveDate.strike = false
                tvEffectiveDate.setTextColor(Color.BLACK)
                tvWage.strike = false
                tvWage.setTextColor(Color.BLACK)
            }
            tvEffectiveDate.text = wage.eprEffectiveDate
            tvWage.text = cf.displayDollars(wage.eprPayRate)
            tvPerFrequency.text = mView.resources.getStringArray(
                R.array.pay_day_frequencies
            )[wage.eprPerPeriod]
            holder.itemView.setOnClickListener {
                gotoWageUpdate(wage)
            }
        }
    }

    private fun gotoWageUpdate(wage: EmployerPayRates) {
        mainViewModel.setPayRate(wage)
        mainViewModel.setEmployer(curEmployer)
        mainViewModel.addCallingFragment(parentTag)
        employerPayRatesFragment.gotoEmployerWageUpdateFragment()
    }
}