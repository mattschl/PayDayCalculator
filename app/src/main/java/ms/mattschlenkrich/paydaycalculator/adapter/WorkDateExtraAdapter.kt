package ms.mattschlenkrich.paydaycalculator.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.databinding.ListWorkDateExtraItemBinding
import ms.mattschlenkrich.paydaycalculator.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import ms.mattschlenkrich.paydaycalculator.ui.paydays.WorkDateAddFragment

class WorkDateExtraAdapter(
    val mainActivity: MainActivity,
    val mView: View,
    private val parentFragment: WorkDateAddFragment,
) : RecyclerView.Adapter<WorkDateExtraAdapter.ExtraViewHolder>() {

    class ExtraViewHolder(
        val itemBinding: ListWorkDateExtraItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<WorkExtraTypes>() {
            override fun areItemsTheSame(
                oldItem: WorkExtraTypes,
                newItem: WorkExtraTypes
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: WorkExtraTypes,
                newItem: WorkExtraTypes
            ): Boolean {
                return oldItem.workExtraTypeId == newItem.workExtraTypeId &&
                        oldItem.wetName == newItem.wetName
            }
        }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExtraViewHolder {
        return ExtraViewHolder(
            ListWorkDateExtraItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ExtraViewHolder, position: Int) {
        val extra = differ.currentList[position]
        holder.itemBinding.apply {
            chkExtra.text = extra.wetName
            chkExtra.isChecked = extra.wetIsDefault
            chkExtra.setOnClickListener {
                chooseSaveOrNot()
                parentFragment.addToExtraList(chkExtra.isChecked, extra)
            }
            btnEdit.visibility = View.GONE
//            holder.itemBinding.btnEdit.setOnClickListener {
//                chooseSaveOrNot()
//            }
        }
    }

    private fun chooseSaveOrNot() {
        AlertDialog.Builder(mView.context)
            .setTitle("Choose the next step")
            .setMessage("In order to add extras, this work date must be saved.")
            .setPositiveButton("Save") { _, _ ->
                parentFragment.saveWorkDate(false)
            }
            .setNegativeButton("Not yet", null)
            .show()

    }
}