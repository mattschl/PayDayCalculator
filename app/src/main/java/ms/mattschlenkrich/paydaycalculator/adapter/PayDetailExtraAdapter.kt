package ms.mattschlenkrich.paydaycalculator.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.common.CommonFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.ListPayDetailExtraItemBinding
import ms.mattschlenkrich.paydaycalculator.model.ExtraAndTotal

class PayDetailExtraAdapter(
    private val creditList: ArrayList<ExtraAndTotal>
) : RecyclerView.Adapter<PayDetailExtraAdapter.CreditViewHolder>() {

    private val cf = CommonFunctions()

    class CreditViewHolder(val itemBinding: ListPayDetailExtraItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreditViewHolder {
        return CreditViewHolder(
            ListPayDetailExtraItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return creditList.size
    }

    override fun onBindViewHolder(holder: CreditViewHolder, position: Int) {
        val credit = creditList[position]
        holder.itemBinding.apply {
            tvExtraDescription.text = credit.extraName
            tvExtraTotal.text = cf.displayDollars(credit.amount)
        }
    }
}