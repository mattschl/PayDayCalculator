package ms.mattschlenkrich.paydaycalculator.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.common.CommonFunctions
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.ListWorkDateExtraItemBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkDateExtras
import ms.mattschlenkrich.paydaycalculator.ui.paydays.WorkDateUpdateFragment

private const val TAG = "WorkDateXtrUpdateCust"

class WorkDateExtraUpdateCustomAdapter(
    val mainActivity: MainActivity,
    val mView: View,
    private val parentFragment: WorkDateUpdateFragment,
    private val workDateExtras: ArrayList<WorkDateExtras>,
) : RecyclerView.Adapter<WorkDateExtraUpdateCustomAdapter.ViewHolder>() {

    private val df = DateFunctions()
    private val cf = CommonFunctions()

    class ViewHolder(
        val itemBinding: ListWorkDateExtraItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListWorkDateExtraItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return workDateExtras.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val extra = workDateExtras[position]
        holder.itemBinding.apply {
            var display =
                "${extra.wdeName} "
            display += if (extra.wdeIsCredit) {
                "add "
            } else {
                "subtract "
            }
            display += if (extra.wdeIsFixed) {
                cf.displayDollars(extra.wdeValue)
            } else {
                cf.displayPercentFromDouble(extra.wdeValue)
            }
            chkExtra.text = display
            chkExtra.isChecked = !extra.wdeIsDeleted

            chkExtra.setOnClickListener {
                if (chkExtra.isChecked) {
                    activateExtra(extra)
                } else {
                    deleteExtra(extra)
                }
            }
        }
    }

    private fun deleteExtra(extra: WorkDateExtras) {
        mainActivity.payDayViewModel.deleteWorkDateExtra(
            extra.wdeName, extra.wdeWorkDateId, extra.wdeUpdateTime
        )
        parentFragment.fillExtras()
    }

    private fun activateExtra(extra: WorkDateExtras) {
        Log.d(TAG, "activateExtra is entered")
        if (extra.workDateExtraId != 0L) {
            Log.d(TAG, "UPDATING the extra")
            mainActivity.payDayViewModel.updateWorkDateExtra(
                WorkDateExtras(
                    extra.workDateExtraId,
                    extra.wdeWorkDateId,
                    extra.wdeExtraTypeId,
                    extra.wdeName,
                    extra.wdeAppliesTo,
                    extra.wdeAttachTo,
                    extra.wdeValue,
                    extra.wdeIsFixed,
                    extra.wdeIsCredit,
                    false,
                    df.getCurrentTimeAsString()
                )
            )
        } else {
            Log.d(TAG, "ADDING the extra")
            mainActivity.payDayViewModel.insertWorkDateExtra(
                WorkDateExtras(
                    cf.generateId(),
                    extra.wdeWorkDateId,
                    extra.wdeExtraTypeId,
                    extra.wdeName,
                    extra.wdeAppliesTo,
                    extra.wdeAttachTo,
                    extra.wdeValue,
                    extra.wdeIsFixed,
                    extra.wdeIsCredit,
                    false,
                    df.getCurrentTimeAsString()
                )
            )
        }
        parentFragment.fillExtras()
    }

}