package ms.mattschlenkrich.paydaycalculator.ui.workorder.workorderHistory.adpater

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.MaterialInSequence
import ms.mattschlenkrich.paydaycalculator.databinding.ListSingleItemBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import ms.mattschlenkrich.paydaycalculator.ui.workorder.workorderHistory.WorkOrderHistoryUpdateFragmentDirections

class WorkOrderHistoryMaterialAdapter(
    val mainActivity: MainActivity,
    val mView: View,
) : RecyclerView.Adapter<WorkOrderHistoryMaterialAdapter.ViewHolder>() {

    //    private val df = DateFunctions()
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
                return oldItem.workOrderHistoryMaterialId ==
                        newItem.workOrderHistoryMaterialId &&
                        oldItem.workOrderHistoryId ==
                        newItem.workOrderHistoryId &&
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
                "${nf.getNumberFromDouble(material.mQty)} -  " +
                        material.mName
            tvDisplay.text = display
        }
        holder.itemView.setOnClickListener {
            chooseOptions(material)
        }
    }

    private fun chooseOptions(material: MaterialInSequence) {
        AlertDialog.Builder(mView.context)
            .setTitle(
                mView.context.getString(R.string.choose_option_for) +
                        material.mName
            )
            .setItems(
                arrayOf(
                    mView.context.getString(R.string.remove_this_item),
                    mView.context.getString(R.string.change_the_quantity),
                    mView.context.getString(R.string.edit_the_material_in_the_database),
                    mView.context.getString(R.string.cancel)
                )
            ) { _, pos ->
                when (pos) {
                    0 -> {
                        removeMaterial(material)
                    }


                    1 -> {
                        changeQuantity(material)
                    }

                    2 -> {
                        editMaterial(material)
                    }

                    else -> {
                        //No action
                    }
                }
            }.show()

    }

    private fun removeMaterial(material: MaterialInSequence) {
        mainActivity.workOrderViewModel.removeWorkOrderHistoryMaterial(
            material.workOrderHistoryMaterialId
        )
    }

    private fun changeQuantity(material: MaterialInSequence) {
        mainActivity.mainViewModel.setMaterialInSequence(material)
        gotoMaterialQuantityUpdateFragment()
    }

    private fun gotoMaterialQuantityUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections
                .actionWorkOrderHistoryUpdateFragmentToMaterialQuantityUpdateFragment()
        )
    }

    private fun editMaterial(material: MaterialInSequence) {
        mainActivity.workOrderViewModel.getMaterial(
            material.materialId
        ).observe(mView.findViewTreeLifecycleOwner()!!) { mMaterial ->
            mainActivity.mainViewModel.setMaterial(mMaterial)
            gotoMaterialUpdateFragment()
        }

    }

    private fun gotoMaterialUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections
                .actionWorkOrderHistoryUpdateFragmentToMaterialUpdateFragment()
        )
    }
}
