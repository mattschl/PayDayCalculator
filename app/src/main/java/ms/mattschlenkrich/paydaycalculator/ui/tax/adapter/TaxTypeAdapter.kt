package ms.mattschlenkrich.paydaycalculator.ui.tax.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.database.model.tax.TaxTypes
import ms.mattschlenkrich.paydaycalculator.databinding.ListSingleItemBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import ms.mattschlenkrich.paydaycalculator.ui.tax.TaxTypeFragmentDirections

class TaxTypeAdapter(
    private val mainActivity: MainActivity,
    private val mView: View
) : RecyclerView.Adapter<TaxTypeAdapter.TaxTypeViewHolder>() {

    class TaxTypeViewHolder(val itemBinding: ListSingleItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<TaxTypes>() {
            override fun areContentsTheSame(oldItem: TaxTypes, newItem: TaxTypes): Boolean {
                return oldItem.taxType == newItem.taxType
            }

            override fun areItemsTheSame(oldItem: TaxTypes, newItem: TaxTypes): Boolean {
                return oldItem == newItem
            }
        }
    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaxTypeViewHolder {
        return TaxTypeViewHolder(
            ListSingleItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: TaxTypeViewHolder, position: Int) {
        val taxType = differ.currentList[position]
        holder.itemBinding.apply {
            var display = taxType.taxType
            if (taxType.ttIsDeleted) {
                display += mView.context.getString(R.string.deleted)
                tvDisplay.setTextColor(Color.RED)
            } else {
                tvDisplay.setTextColor(Color.BLACK)
            }
            tvDisplay.text = display
            holder.itemView.setOnLongClickListener {
                gotoTaxTypeUpdate(taxType)
                true
            }
        }
    }

    private fun gotoTaxTypeUpdate(taxType: TaxTypes?) {
        mainActivity.mainViewModel.setTaxType(taxType)
        gotoTaxTypeUpdateFragment()
    }

    private fun gotoTaxTypeUpdateFragment() {
        mView.findNavController().navigate(
            TaxTypeFragmentDirections
                .actionTaxTypeFragmentToTaxTypeUpdateFragment()
        )
    }
}