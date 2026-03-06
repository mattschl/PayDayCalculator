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
import ms.mattschlenkrich.paycalculator.database.model.workorder.Material
import ms.mattschlenkrich.paycalculator.databinding.ListSingleItemBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.workorder.materials.MaterialMergeFragment

class MaterialMergedAdapter(
    private val mainActivity: MainActivity,
    private val mView: View,
    private val materialMergeFragment: MaterialMergeFragment
) : RecyclerView.Adapter<MaterialMergedAdapter.MaterialViewHolder>() {

    val mainViewmodel = mainActivity.mainViewModel

    class MaterialViewHolder(val itemBinding: ListSingleItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack = object : DiffUtil.ItemCallback<Material>() {
        override fun areItemsTheSame(
            p0: Material,
            p1: Material
        ): Boolean {
            return p1 == p0
        }

        override fun areContentsTheSame(
            p0: Material,
            p1: Material
        ): Boolean {
            return p1.materialId == p0.materialId && p1.mName == p0.mName
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
        val material = differ.currentList[position]
        holder.itemBinding.apply {
            var display = material.mName
            if (material.mIsDeleted) {
                display += mView.context.getString(R.string._deleted_)
                tvDisplay.setTextColor(Color.RED)
            } else {
                tvDisplay.setTextColor(Color.BLACK)
            }
            tvDisplay.text = display
        }
        holder.itemView.setOnClickListener {
            chooseOptions(material)

        }
    }

    private fun chooseOptions(material: Material) {
        AlertDialog.Builder(mView.context)
            .setTitle("Choose if this is a parent or child")
            .setPositiveButton("Parent") { _, _ ->
                materialMergeFragment.chooseAsParent(material)
            }
            .setNegativeButton("Child") { _, _ ->
                materialMergeFragment.chooseAsChild(material)
            }
            .show()
    }
}