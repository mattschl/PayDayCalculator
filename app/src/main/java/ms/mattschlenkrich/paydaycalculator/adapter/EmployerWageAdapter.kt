package ms.mattschlenkrich.paydaycalculator.adapter

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.ListWagesItemBinding
import ms.mattschlenkrich.paydaycalculator.model.employer.EmployerPayRates
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import ms.mattschlenkrich.paydaycalculator.ui.employer.EmployerPayRatesFragmentDirections

class EmployerWageAdapter(
    private val mainActivity: MainActivity,
    private val mView: View,
    private val curEmployer: Employers,
    private val parentTag: String,
) : RecyclerView.Adapter<EmployerWageAdapter.WageViewHolder>() {

    private val cf = NumberFunctions()
    private inline var TextView.strike: Boolean
        set(visible) {
            paintFlags = if (visible) paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            else paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        get() = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG == Paint.STRIKE_THRU_TEXT_FLAG

//    private val df = DateFunctions()

    class WageViewHolder(val itemBinding: ListWagesItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<EmployerPayRates>() {
            override fun areItemsTheSame(
                oldItem: EmployerPayRates,
                newItem: EmployerPayRates
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: EmployerPayRates,
                newItem: EmployerPayRates
            ): Boolean {
                return oldItem.employerPayRateId == newItem.employerPayRateId &&
                        oldItem.eprEmployerId == newItem.eprEmployerId &&
                        oldItem.eprPayRate == newItem.eprPayRate &&
                        oldItem.eprPerPeriod == newItem.eprPerPeriod
            }
        }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WageViewHolder {
        return WageViewHolder(
            ListWagesItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
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
                R.array.pay_per_frequencies
            )[wage.eprPerPeriod]
            holder.itemView.setOnClickListener {
                gotoWageUpdate(wage)
            }
        }
    }

    private fun gotoWageUpdate(wage: EmployerPayRates) {
        mainActivity.mainViewModel.setPayRate(wage)
        mainActivity.mainViewModel.setEmployer(curEmployer)
        mainActivity.mainViewModel.addCallingFragment(parentTag)
        mView.findNavController().navigate(
            EmployerPayRatesFragmentDirections
                .actionEmployerPayRatesFragmentToEmployerWageUpdateFragment()
        )
    }
}