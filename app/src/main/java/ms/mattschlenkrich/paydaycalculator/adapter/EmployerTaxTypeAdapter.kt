package ms.mattschlenkrich.paydaycalculator.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.databinding.ListEmployerTaxItemBinding
import ms.mattschlenkrich.paydaycalculator.model.EmployerTaxTypes
import ms.mattschlenkrich.paydaycalculator.ui.employer.EmployerUpdateFragment
import ms.mattschlenkrich.paydaycalculator.ui.employer.EmployerUpdateFragmentDirections

class EmployerTaxTypeAdapter(
    private val mainActivity: MainActivity,
    private val mView: View,
    private val parentFragment: EmployerUpdateFragment,
) : RecyclerView.Adapter<EmployerTaxTypeAdapter.EmployerTaxViewHolder>() {

    class EmployerTaxViewHolder(
        val itemBinding: ListEmployerTaxItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<EmployerTaxTypes>() {
            override fun areItemsTheSame(
                oldItem: EmployerTaxTypes,
                newItem: EmployerTaxTypes
            ): Boolean {
                return oldItem.etrEmployerId == newItem.etrEmployerId &&
                        oldItem.etrTaxType == newItem.etrTaxType
            }

            override fun areContentsTheSame(
                oldItem: EmployerTaxTypes,
                newItem: EmployerTaxTypes
            ): Boolean {
                return oldItem == newItem
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
        holder.itemBinding.chEmployerTax.text = employerTaxType.etrTaxType
        holder.itemBinding.chEmployerTax.isChecked = employerTaxType.etrInclude
        holder.itemBinding.chEmployerTax.setOnClickListener {
            mainActivity.workTaxViewModel.updateEmployerTaxIncluded(
                employerTaxType.etrEmployerId, employerTaxType.etrTaxType,
                holder.itemBinding.chEmployerTax.isChecked
            )
            parentFragment.fillTaxes(employerTaxType.etrEmployerId)
        }
        holder.itemBinding.btnEdit.setOnClickListener {
            mainActivity.mainViewModel.setTaxTypeString(employerTaxType.etrTaxType)
            mView.findNavController().navigate(
                EmployerUpdateFragmentDirections
                    .actionEmployerUpdateFragmentToTaxRulesFragment()
            )
        }
    }
}