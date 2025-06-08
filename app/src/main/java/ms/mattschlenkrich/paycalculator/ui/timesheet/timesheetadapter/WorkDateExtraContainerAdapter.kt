package ms.mattschlenkrich.paycalculator.ui.timesheet.timesheetadapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.database.model.extras.ExtraContainer
import ms.mattschlenkrich.paycalculator.databinding.ListWorkDateExtraTimeSheetBinding

class WorkDateExtraContainerAdapter(
    private val extraContainerList: List<ExtraContainer>,
    private val mView: View,
) : RecyclerView.Adapter<WorkDateExtraContainerAdapter.DateExtrasHolder>() {

    private val cf = NumberFunctions()

    class DateExtrasHolder(
        val itemBinding: ListWorkDateExtraTimeSheetBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateExtrasHolder {
        return DateExtrasHolder(
            ListWorkDateExtraTimeSheetBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return extraContainerList.size
    }

    override fun onBindViewHolder(holder: DateExtrasHolder, position: Int) {
        val extra = extraContainerList[position]
        holder.itemBinding.apply {
            var display = if (position > 0) mView.context.getString(R.string.pipe)
            else ""
            display += extra.extraName + " " + cf.displayDollars(extra.amount)
            tvExtra.text = display
        }
        holder.itemView.setOnClickListener {
            gotoEditDate(extra.workDateExtra!!.wdeWorkDateId)
        }
    }

    private fun gotoEditDate(wdeWorkDateId: Long) {
        //TODO: create the navigation action
    }
}