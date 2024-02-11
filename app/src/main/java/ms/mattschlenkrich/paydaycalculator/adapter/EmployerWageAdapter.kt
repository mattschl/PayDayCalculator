package ms.mattschlenkrich.paydaycalculator.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.CommonFunctions
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.ListWagesItemBinding
import ms.mattschlenkrich.paydaycalculator.model.EmployerPayRates

class EmployerWageAdapter(
    private val mainActivity: MainActivity,
    private val mView: View,
) : RecyclerView.Adapter<EmployerWageAdapter.WageViewHolder>() {

    private val cf = CommonFunctions()
    private val df = DateFunctions()

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
            tvEffectiveDate.text = wage.eprEffectiveDate
            tvWage.text = cf.displayDollars(wage.eprPayRate)
            tvPerFrequency.text = mView.resources.getStringArray(
                R.array.extra_frequencies
            )[wage.eprPerPeriod]
        }
    }
}