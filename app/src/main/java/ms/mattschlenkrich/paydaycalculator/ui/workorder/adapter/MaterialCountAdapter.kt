package ms.mattschlenkrich.paydaycalculator.ui.workorder.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.MaterialAndQuantity
import ms.mattschlenkrich.paydaycalculator.databinding.ListSingleItemBinding

class MaterialCountAdapter(
    val materialCount: List<MaterialAndQuantity>
) : RecyclerView.Adapter<MaterialCountAdapter.ViewHolder>() {

    val nf = NumberFunctions()

    class ViewHolder(
        val itemBinding: ListSingleItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListSingleItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return materialCount.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val material = materialCount[position]
        holder.itemBinding.apply {
            var display =
                nf.getNumberFromDouble(material.quantity)
            display += " - ${material.name}"
            tvDisplay.text = display
        }
    }

}