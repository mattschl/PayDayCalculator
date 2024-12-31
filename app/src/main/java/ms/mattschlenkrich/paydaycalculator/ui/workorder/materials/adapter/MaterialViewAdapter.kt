package ms.mattschlenkrich.paydaycalculator.ui.workorder.materials.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.Material
import ms.mattschlenkrich.paydaycalculator.databinding.ListSingleItemBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import ms.mattschlenkrich.paydaycalculator.ui.workorder.materials.MaterialViewFragmentDirections

class MaterialViewAdapter(
    private val mainActivity: MainActivity,
    private val mView: View,
    private val parentTag: String,
) : RecyclerView.Adapter<MaterialViewAdapter.MaterialViewHolder>() {

    class MaterialViewHolder(val itemBinding: ListSingleItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<Material>() {
            override fun areItemsTheSame(oldItem: Material, newItem: Material): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Material, newItem: Material): Boolean {
                return oldItem.materialId == newItem.materialId &&
                        oldItem.mName == newItem.mName
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
                display += mView.context.getString(R.string.deleted)
                tvDisplay.setTextColor(Color.RED)
            } else {
                tvDisplay.setTextColor(Color.BLACK)
            }
            tvDisplay.text = display
        }
        holder.itemView.setOnClickListener {
            gotoUpdateMaterial(material)
        }
    }

    private fun gotoUpdateMaterial(material: Material) {
        mainActivity.mainViewModel.setCallingFragment(parentTag)
        mainActivity.mainViewModel.setMaterial(material)
        gotoMaterialUpdateFragment()
    }

    private fun gotoMaterialUpdateFragment() {
        mView.findNavController().navigate(
            MaterialViewFragmentDirections
                .actionMaterialViewFragmentToMaterialUpdateFragment()
        )
    }
}