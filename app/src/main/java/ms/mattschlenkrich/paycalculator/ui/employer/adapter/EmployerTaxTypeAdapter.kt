package ms.mattschlenkrich.paycalculator.ui.employer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paycalculator.database.model.employer.EmployerTaxTypes
import ms.mattschlenkrich.paycalculator.databinding.ListEmployerTaxItemBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.employer.EmployerUpdateFragment

class EmployerTaxTypeAdapter(
    val mainActivity: MainActivity,
    private val employerUpdateFragment: EmployerUpdateFragment,
) : RecyclerView.Adapter<EmployerTaxTypeAdapter.EmployerTaxViewHolder>() {

    val mainViewModel = mainActivity.mainViewModel
    val workTaxViewModel = mainActivity.workTaxViewModel

    class EmployerTaxViewHolder(
        val itemBinding: ListEmployerTaxItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<EmployerTaxTypes>() {
            override fun areItemsTheSame(
                oldItem: EmployerTaxTypes,
                newItem: EmployerTaxTypes
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: EmployerTaxTypes,
                newItem: EmployerTaxTypes
            ): Boolean {
                return oldItem.etrEmployerId == newItem.etrEmployerId &&
                        oldItem.etrTaxType == newItem.etrTaxType
            }
        }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployerTaxViewHolder {
        return EmployerTaxViewHolder(
            ListEmployerTaxItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: EmployerTaxViewHolder, position: Int) {
        val employerTaxType = differ.currentList[position]
        holder.itemBinding.apply {
            chkEmployerTax.text = employerTaxType.etrTaxType
            chkEmployerTax.isChecked = employerTaxType.etrInclude
            chkEmployerTax.setOnClickListener {
                workTaxViewModel.updateEmployerTaxIncluded(
                    employerTaxType.etrEmployerId, employerTaxType.etrTaxType,
                    chkEmployerTax.isChecked
                )
                employerUpdateFragment.populateTaxes(employerTaxType.etrEmployerId)
            }
            btnEdit.setOnClickListener {
                gotoTaxRules(employerTaxType)
            }
        }
    }

    private fun gotoTaxRules(employerTaxType: EmployerTaxTypes) {
        mainViewModel.setTaxTypeString(employerTaxType.etrTaxType)
        employerUpdateFragment.gotoRulesFragment()
    }

}