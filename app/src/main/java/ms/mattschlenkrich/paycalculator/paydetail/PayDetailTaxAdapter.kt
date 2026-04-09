package ms.mattschlenkrich.paycalculator.paydetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.TaxAndAmount
import ms.mattschlenkrich.paycalculator.databinding.ListPayDetailTaxItemBinding

class PayDetailTaxAdapter(
    private val taxList: List<TaxAndAmount>
) : RecyclerView.Adapter<PayDetailTaxAdapter.CreditViewHolder>() {

    private val nf = NumberFunctions()

    class CreditViewHolder(val itemBinding: ListPayDetailTaxItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreditViewHolder {
        return CreditViewHolder(
            ListPayDetailTaxItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return taxList.size
    }

    override fun onBindViewHolder(holder: CreditViewHolder, position: Int) {
        val tax = taxList[position]
        holder.itemBinding.apply {
            tvTaxName.text = tax.taxType
            tvTaxTotal.text = nf.displayDollars(tax.amount)
        }
    }
}