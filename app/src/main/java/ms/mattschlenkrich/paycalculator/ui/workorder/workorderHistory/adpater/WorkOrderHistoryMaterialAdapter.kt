package ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory.adpater

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.database.model.workorder.MaterialInSequence
import ms.mattschlenkrich.paycalculator.databinding.ListSingleItemBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory.IWorkOrderHistoryUpdateFragment

class WorkOrderHistoryMaterialAdapter(
    val mainActivity: MainActivity,
    val mView: View,
    private val parentFragment: String,
    private val workOrderHistoryUpdateFragment: IWorkOrderHistoryUpdateFragment,
) : RecyclerView.Adapter<WorkOrderHistoryMaterialAdapter.ViewHolder>() {

    //    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private val mainViewModel = mainActivity.mainViewModel
    private val workOrderViewModel = mainActivity.workOrderViewModel

    class ViewHolder(
        val itemBinding: ListSingleItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallback = object : DiffUtil.ItemCallback<MaterialInSequence>() {
        override fun areItemsTheSame(
            oldItem: MaterialInSequence, newItem: MaterialInSequence
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: MaterialInSequence, newItem: MaterialInSequence
        ): Boolean {
            return oldItem.workOrderHistoryMaterialId == newItem.workOrderHistoryMaterialId && oldItem.workOrderHistoryId == newItem.workOrderHistoryId && oldItem.materialId == newItem.materialId && oldItem.mName == newItem.mName && oldItem.mQty == newItem.mQty && oldItem.mSequence == newItem.mSequence
        }

    }
    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListSingleItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val material = differ.currentList[position]
        holder.itemBinding.apply {
            val display = "${nf.getNumberFromDouble(material.mQty)} -  " + material.mName
            tvDisplay.text = display
        }
        holder.itemView.setOnClickListener {
            chooseOptions(material)
        }
    }

    private fun chooseOptions(material: MaterialInSequence) {
        AlertDialog.Builder(mView.context).setTitle(
            mView.context.getString(R.string.choose_option_for) + material.mName
        ).setItems(
            arrayOf(
                mView.context.getString(R.string.update_this_material_or_quantity_for_this_history),
                mView.context.getString(R.string.change_the_quantity),
                mView.context.getString(R.string.edit_the_material_in_the_database),
                mView.context.getString(R.string.remove_this_item),
                mView.context.getString(R.string.cancel)
            )
        ) { _, pos ->
            when (pos) {
                0 -> {
                    updateMaterialInHistory(material)
                }

                1 -> {
                    changeQuantity(material)
                }

                2 -> {
                    editMaterial(material)
                }

                3 -> {
                    removeMaterialFromHistory(material)
                }

                else -> { //No action
                }
            }
        }.show()

    }

    private fun updateMaterialInHistory(material: MaterialInSequence) {
        mainViewModel.setMaterialInSequence(material)
        mainViewModel.addCallingFragment(parentFragment)
        workOrderHistoryUpdateFragment.gotoWorkOrderHistoryMaterialUpdateFragment()
    }

    private fun removeMaterialFromHistory(material: MaterialInSequence) {
        workOrderViewModel.removeWorkOrderHistoryMaterial(
            material.workOrderHistoryMaterialId
        )
    }

    private fun changeQuantity(material: MaterialInSequence) {
        workOrderHistoryUpdateFragment.setTempWorkOrderHistoryInfo()
        mainViewModel.setMaterialInSequence(material)
        workOrderHistoryUpdateFragment.gotoMaterialQuantityUpdateFragment()
    }

    private fun editMaterial(material: MaterialInSequence) {
        workOrderHistoryUpdateFragment.setTempWorkOrderHistoryInfo()
        workOrderViewModel.getMaterial(material.materialId)
            .observe(mView.findViewTreeLifecycleOwner()!!) { mMaterial ->
                mainViewModel.setMaterial(mMaterial)
                workOrderHistoryUpdateFragment.gotoMaterialUpdateFragment()
            }

    }
}
