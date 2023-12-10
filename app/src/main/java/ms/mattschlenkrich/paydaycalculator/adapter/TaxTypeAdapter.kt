package ms.mattschlenkrich.paydaycalculator.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.databinding.ListTaxTypeItemBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkTaxTypes

class TaxTypeAdapter(
    private val mainActivity: MainActivity,
    private val mView: View
) : RecyclerView.Adapter<TaxTypeAdapter.TaxTypeViewHolder>() {

    class TaxTypeViewHolder(val itemBinding: ListTaxTypeItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<WorkTaxTypes>() {
            override fun areContentsTheSame(oldItem: WorkTaxTypes, newItem: WorkTaxTypes): Boolean {
                return oldItem.workTaxType == newItem.workTaxType &&
                        oldItem.wttIsDeleted == newItem.wttIsDeleted
            }

            override fun areItemsTheSame(oldItem: WorkTaxTypes, newItem: WorkTaxTypes): Boolean {
                return oldItem == newItem
            }
        }
    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaxTypeViewHolder {
        return TaxTypeViewHolder(
            ListTaxTypeItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: TaxTypeViewHolder, position: Int) {
        val taxType = differ.currentList[position]
        holder.itemBinding.tvTaxType.text = taxType.workTaxType

    }
}