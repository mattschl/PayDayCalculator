package ms.mattschlenkrich.paydaycalculator.ui.paydays.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.FRAG_WORK_DATE_UPDATE
import ms.mattschlenkrich.paydaycalculator.database.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paydaycalculator.databinding.ListWorkDateExtraItemBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import ms.mattschlenkrich.paydaycalculator.ui.paydays.WorkDateAddFragment

class WorkDateDefaultExtraAdapter(
    val mainActivity: MainActivity,
    val mView: View,
    private val workDateAddFragment: WorkDateAddFragment,
) : RecyclerView.Adapter<WorkDateDefaultExtraAdapter.ExtraViewHolder>() {

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
                confirmSaveWorkDate(chkExtra.isChecked, extra)
            }
            btnEdit.visibility = View.GONE
        }
    }

    private fun confirmSaveWorkDate(isChecked: Boolean, extra: WorkExtraTypes) {
        AlertDialog.Builder(mView.context)
            .setTitle(mView.context.getString(R.string.choose_the_next_step))
            .setMessage(
                mView.context.getString(R.string.in_order_to_add_extras_this_work_date_must_be_saved)
            )
            .setPositiveButton(
                mView.context.getString(R.string.save)
            ) { _, _ ->
                workDateAddFragment.saveWorkDate(FRAG_WORK_DATE_UPDATE)
                workDateAddFragment.addToExtraList(isChecked, extra)
            }
            .setNegativeButton(mView.context.getString(R.string.go_back), null)
            .show()

    }
}