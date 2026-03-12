package ms.mattschlenkrich.paycalculator.ui.workorder.materials.adapter

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.database.model.workorder.merged.MaterialAndChild
import ms.mattschlenkrich.paycalculator.databinding.ListSingleItemBinding
import ms.mattschlenkrich.paycalculator.ui.workorder.materials.MaterialMergeFragment

class MaterialChildrenAdapter(
    private val mView: View,
    private val materialMergeFragment: MaterialMergeFragment
) : RecyclerView.Adapter<MaterialChildrenAdapter.MaterialViewHolder>() {

    class MaterialViewHolder(val itemBinding: ListSingleItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack = object : DiffUtil.ItemCallback<MaterialAndChild>() {
        override fun areItemsTheSame(
            p0: MaterialAndChild,
            p1: MaterialAndChild
        ): Boolean {
            return p0 == p1
        }

        override fun areContentsTheSame(
            p0: MaterialAndChild,
            p1: MaterialAndChild
        ): Boolean {
            return p0.materialMerged.materialMergeId == p1.materialMerged.materialMergeId &&
                    p0.materialParent.materialId == p1.materialParent.materialId &&
                    p0.materialChild.materialId == p1.materialChild.materialId
        }
    }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialViewHolder {
        return MaterialViewHolder(
            ListSingleItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: MaterialViewHolder, position: Int) {
        val materialAndChild = differ.currentList[position]
        holder.itemBinding.apply {
            var display = materialAndChild.materialChild.mName
            if (materialAndChild.materialChild.mIsDeleted) {
                display += mView.context.getString(R.string._deleted_)
                tvDisplay.setTextColor(Color.RED)
            } else {
                tvDisplay.setTextColor(Color.BLACK)
            }
            tvDisplay.text = display
        }
        holder.itemView.setOnClickListener {
            chooseOptions(materialAndChild)

        }
    }

    private fun chooseOptions(materialAndChild: MaterialAndChild) {
        AlertDialog.Builder(mView.context)
            .setTitle("Remove as Child")
            .setPositiveButton("Remove") { _, _ ->
                materialMergeFragment.removeMaterialAsChild(materialAndChild)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}