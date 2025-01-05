package ms.mattschlenkrich.paycalculator.ui.workorder.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.database.model.workorder.MaterialAndQuantity
import ms.mattschlenkrich.paycalculator.databinding.ListSingleItemBinding

class MaterialCountAdapter(
    private val materialCount: List<MaterialAndQuantity>,
    private val mView: View,
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
            val display =
                nf.getNumberFromDouble(material.quantity) +
                        mView.context.getString(R.string.hyphen) +
                        material.name
            tvDisplay.text = display
        }
    }

}