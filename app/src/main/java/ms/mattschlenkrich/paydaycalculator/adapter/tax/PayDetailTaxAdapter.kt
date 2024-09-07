package ms.mattschlenkrich.paydaycalculator.adapter.tax

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.database.model.extras.ExtraAndTotal
import ms.mattschlenkrich.paydaycalculator.databinding.ListPayDetailTaxItemBinding

class PayDetailTaxAdapter(
    private val creditList: ArrayList<ExtraAndTotal>
) : RecyclerView.Adapter<PayDetailTaxAdapter.CreditViewHolder>() {

    private val cf = NumberFunctions()

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
        return creditList.size
    }

    override fun onBindViewHolder(holder: CreditViewHolder, position: Int) {
        val tax = creditList[position]
        holder.itemBinding.apply {
            tvTaxName.text = tax.extraName
            tvTaxTotal.text = cf.displayDollars(tax.amount)
        }
    }
}