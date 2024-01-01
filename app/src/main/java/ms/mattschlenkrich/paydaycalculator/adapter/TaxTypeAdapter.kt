package ms.mattschlenkrich.paydaycalculator.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.databinding.ListSingleItemBinding
import ms.mattschlenkrich.paydaycalculator.model.TaxTypes
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
        var display = taxType.taxType
        if (taxType.ttIsDeleted) {
            display += " *DELETE*"
            holder.itemBinding.tvDisplay.setTextColor(Color.RED)
        } else {
            holder.itemBinding.tvDisplay.setTextColor(Color.BLACK)
        }
        holder.itemBinding.tvDisplay.text = display
        holder.itemView.setOnLongClickListener {
            mainActivity.mainViewModel.setTaxType(taxType)
            mView.findNavController().navigate(
                TaxTypeFragmentDirections
                    .actionTaxTypeFragmentToTaxTypeUpdateFragment()
            )
            true
        }


    }
}