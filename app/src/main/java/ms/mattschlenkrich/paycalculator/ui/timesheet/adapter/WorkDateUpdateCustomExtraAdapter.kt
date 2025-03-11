package ms.mattschlenkrich.paycalculator.ui.timesheet.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDateExtras
import ms.mattschlenkrich.paycalculator.databinding.ListWorkDateExtraItemBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.timesheet.workdate.WorkDateUpdateFragment

class WorkDateUpdateCustomExtraAdapter(
    private val mainActivity: MainActivity,
    private val mView: View,
    private val workDateUpdateFragment: WorkDateUpdateFragment,
    private val workDateExtras: ArrayList<WorkDateExtras>,
) : RecyclerView.Adapter<WorkDateUpdateCustomExtraAdapter.ViewHolder>() {

    private val df = DateFunctions()
    private val nf = NumberFunctions()

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
                extra.wdeName
            if (extra.wdeIsCredit) {
                display += mView.context.getString(R.string.add_)
                chkExtra.setTextColor(Color.BLACK)
            } else {
                display += mView.context.getString(R.string.subtract_)
                chkExtra.setTextColor(Color.RED)
            }
            display += if (extra.wdeIsFixed) {
                nf.displayDollars(extra.wdeValue)
            } else {
                nf.getPercentStringFromDouble(extra.wdeValue)
            }
            chkExtra.text = display
            chkExtra.isChecked = !extra.wdeIsDeleted
            if (extra.wdeIsDeleted) {
                btnEdit.visibility = View.INVISIBLE
            }
            chkExtra.setOnClickListener {
                if (chkExtra.isChecked) {
                    activateExtra(extra)
                } else {
                    deleteExtra(extra)
                }
            }
            btnEdit.setOnClickListener {
                gotoUpdateWorkDateExtra(extra)
            }
        }
    }

    private fun deleteExtra(extra: WorkDateExtras) {
        mainActivity.payDayViewModel.deleteWorkDateExtra(
            extra.wdeName, extra.wdeWorkDateId, extra.wdeUpdateTime
        )
        workDateUpdateFragment.populateExtras()
    }

    private fun activateExtra(extra: WorkDateExtras) {
        if (extra.workDateExtraId != 0L) {
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
            mainActivity.payDayViewModel.insertWorkDateExtra(
                WorkDateExtras(
                    nf.generateRandomIdAsLong(),
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
        workDateUpdateFragment.populateExtras()
    }

    private fun gotoUpdateWorkDateExtra(extra: WorkDateExtras) {
        mainActivity.mainViewModel.setWorkDateExtra(extra)
        mainActivity.mainViewModel.setWorkDateExtraList(workDateExtras)
        workDateUpdateFragment.gotoWorkDateExtraUpdateFragment()
    }

}