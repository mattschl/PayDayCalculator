package ms.mattschlenkrich.paydaycalculator.adapter.workorders

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.MaterialInSequence
import ms.mattschlenkrich.paydaycalculator.databinding.ListSingleItemBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class WorkOrderHistoryMaterialAdapter(
    val mainActivity: MainActivity,
    val mView: View
) : RecyclerView.Adapter<WorkOrderHistoryMaterialAdapter.ViewHolder>() {

    private val df = DateFunctions()
    private val nf = NumberFunctions()

    class ViewHolder(
        val itemBinding: ListSingleItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallback =
        object : DiffUtil.ItemCallback<MaterialInSequence>() {
            override fun areItemsTheSame(
                oldItem: MaterialInSequence,
                newItem: MaterialInSequence
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: MaterialInSequence,
                newItem: MaterialInSequence
            ): Boolean {
                return oldItem.materialHistoryId ==
                        newItem.materialHistoryId &&
                        oldItem.materialId ==
                        newItem.materialId &&
                        oldItem.mName ==
                        newItem.mName &&
                        oldItem.mQty ==
                        newItem.mQty &&
                        oldItem.mSequence ==
                        newItem.mSequence
            }

        }
    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListSingleItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val material = differ.currentList[position]
        holder.itemBinding.apply {
            val display =
                "${nf.getNumberFromDouble(material.mQty)} " +
                        material.mName
            tvDisplay.text = display
        }
        holder.itemView.setOnClickListener {
            chooseOptions(material)
        }
    }

    private fun chooseOptions(material: MaterialInSequence) {
        AlertDialog.Builder(mView.context)
            .setTitle("Choose an option for ${material.mName}")
            .setItems(
                arrayOf(
                    "Remove this item",
                    "Edit the material in the material list",
                    "Cancel"
                )
            ) { _, pos ->
                when (pos) {
                    0 -> {
                        removeMaterial(material)
                    }

                    1 -> {
                        editMaterial(material)
                    }

                    else -> {
                        //null
                    }
                }
            }.show()

    }

    private fun editMaterial(material: MaterialInSequence) {
        //TODO:
    }

    private fun removeMaterial(material: MaterialInSequence) {
        //TODO:
    }
}
