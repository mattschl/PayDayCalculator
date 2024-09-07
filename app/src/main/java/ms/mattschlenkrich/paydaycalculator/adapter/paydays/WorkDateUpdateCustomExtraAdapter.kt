package ms.mattschlenkrich.paydaycalculator.adapter.paydays

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.WorkDateExtras
import ms.mattschlenkrich.paydaycalculator.databinding.ListWorkDateExtraItemBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import ms.mattschlenkrich.paydaycalculator.ui.paydays.WorkDateUpdateFragment
import ms.mattschlenkrich.paydaycalculator.ui.paydays.WorkDateUpdateFragmentDirections

class WorkDateUpdateCustomExtraAdapter(
    val mainActivity: MainActivity,
    val mView: View,
    private val parentFragment: WorkDateUpdateFragment,
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
                "${extra.wdeName} "
            display += if (extra.wdeIsCredit) {
                "add "
            } else {
                "subtract "
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

    private fun gotoUpdateWorkDateExtra(extra: WorkDateExtras) {
        mainActivity.mainViewModel.setWorkDateExtra(extra)
        mainActivity.mainViewModel.setWorkDateExtraList(workDateExtras)
        mView.findNavController().navigate(
            WorkDateUpdateFragmentDirections
                .actionWorkDateUpdateFragmentToWorkDateExtraUpdateFragment()

        )
    }

    private fun deleteExtra(extra: WorkDateExtras) {
        mainActivity.payDayViewModel.deleteWorkDateExtra(
            extra.wdeName, extra.wdeWorkDateId, extra.wdeUpdateTime
        )
        parentFragment.populateExtras()
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
        parentFragment.populateExtras()
    }

}