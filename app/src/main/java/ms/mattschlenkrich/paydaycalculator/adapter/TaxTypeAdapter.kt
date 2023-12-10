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
import ms.mattschlenkrich.paydaycalculator.databinding.ListTaxTypeItemBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkTaxTypes
import ms.mattschlenkrich.paydaycalculator.ui.tax.TaxTypeFragmentDirections

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
        var disp = taxType.workTaxType
        if (taxType.wttIsDeleted) {
            disp += "*Deleted*"
            holder.itemBinding.tvTaxType.setTextColor(Color.RED)
        } else {
            holder.itemBinding.tvTaxType.setTextColor(Color.BLACK)
        }
        holder.itemBinding.tvTaxType.text = disp
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