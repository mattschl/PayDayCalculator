package ms.mattschlenkrich.paycalculator.ui.timesheet.timesheetadapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_100
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paycalculator.databinding.ListWorkDateBinding
import ms.mattschlenkrich.paycalculator.logic.payfunctions.WorkDateExtraContainerCalculations
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.timesheet.ITimeSheetFragment
import ms.mattschlenkrich.paycalculator.ui.timesheet.TimeSheetFragmentDirections

class WorkDateAdapter(
    private val mainActivity: MainActivity,
    private val curCutoff: String,
    private val curEmployer: Employers,
    private val wage: Double,
    private val mView: View,
    private val timeSheetFragment: ITimeSheetFragment
) : RecyclerView.Adapter<WorkDateAdapter.WorkDateViewHolder>() {

    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private val mainViewModel = mainActivity.mainViewModel
    private val payDayViewModel = mainActivity.payDayViewModel
    private val workOrderViewModel = mainActivity.workOrderViewModel
    private val mainScope = CoroutineScope(Dispatchers.Main)

    class WorkDateViewHolder(val itemBinding: ListWorkDateBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack = object : DiffUtil.ItemCallback<WorkDates>() {
        override fun areItemsTheSame(oldItem: WorkDates, newItem: WorkDates): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: WorkDates, newItem: WorkDates): Boolean {
            return oldItem.workDateId == newItem.workDateId && oldItem.wdDate == newItem.wdDate
        }
    }
    val differ = AsyncListDiffer(this, differCallBack)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkDateViewHolder {
        return WorkDateViewHolder(
            ListWorkDateBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }


    override fun onBindViewHolder(holder: WorkDateViewHolder, position: Int) {
        val workDate = differ.currentList[position]
        var display = df.getDisplayDate(workDate.wdDate)
        holder.itemBinding.apply {
            val calculations = WorkDateExtraContainerCalculations(
                mainActivity, workDate, wage,
            )
            if (workDate.wdIsDeleted) {
                display += mView.context.getString(R.string._deleted_)
                tvWorkDate.setTextColor(Color.RED)
            } else {
                tvWorkDate.setTextColor(Color.BLACK)
            }
            tvWorkDate.text = display
            display = ""
            if (workDate.wdRegHours > 0) {
                display =
                    nf.getNumberFromDouble(workDate.wdRegHours) + mView.context.getString(R.string.hrs)
            }
            if (workDate.wdOtHours > 0) {
                if (display.isNotBlank()) display += mView.context.getString(R.string.pipe)
                display += nf.getNumberFromDouble(workDate.wdOtHours) + mView.context.getString(R.string.ot_hrs)
            }
            if (workDate.wdDblOtHours > 0) {
                if (display.isNotBlank()) display += mView.context.getString(R.string.pipe)
                display += nf.getNumberFromDouble(workDate.wdDblOtHours) + mView.context.getString(R.string.dbl_ot_hrs)
            }
            if (workDate.wdStatHours > 0) {
                if (display.isNotBlank()) display += " | "
                display += nf.getNumberFromDouble(workDate.wdStatHours) + mView.context.getString(R.string.stat_vacation_hrs)
            }
            if (!workDate.wdNote.isNullOrBlank()) {
                if (display.isNotBlank()) display += " - "
                display += workDate.wdNote
            }
            if (display.isBlank()) {
                display += mView.context.getString(R.string.no_time_entered)
            }
            tvHours.text = display
//            val extrasAdapter = TimeSheetWorkDateExtraAdapter(mView)
            mainScope.launch {
                val extrasAdapter = WorkDateExtraContainerAdapter(
                    calculations.getExtraContainerList(), mView
                )
                delay(WAIT_100)
                rvExtras.apply {
                    layoutManager = LinearLayoutManager(
                        mView.context, RecyclerView.HORIZONTAL, false
                    )
                    adapter = extrasAdapter
                }
            }
            holder.itemView.setOnClickListener {
                gotoWorkDateUpdate(workDate)
            }
            holder.itemView.setOnLongClickListener {
                chooseOptionsForDate(workDate)
                true
            }
        }
    }

    private fun chooseOptionsForDate(workDate: WorkDates) {
        AlertDialog.Builder(mView.context).setTitle(
            mView.context.getString(R.string.choose_an_action_for) + df.getDisplayDate(workDate.wdDate)
        ).setItems(
            arrayOf(
                mView.context.getString(R.string.open_this_date),
                mView.context.getString(R.string.delete_this_date)
            )
        ) { _, pos ->
            when (pos) {
                0 -> {
                    gotoWorkDateUpdate(workDate)
                }

                1 -> {
                    confirmDeleteWorkDate(workDate)
                }

                else -> {
                    //do nothing
                }
            }
        }.setNegativeButton(
            mView.context.getString(R.string.cancel), null
        ).show()
    }

    private fun confirmDeleteWorkDate(workDate: WorkDates) {
        android.app.AlertDialog.Builder(mView.context).setTitle(
            mView.context.getString(R.string.are_you_sure_you_want_to_delete) + df.getDisplayDate(
                workDate.wdDate
            )
        ).setMessage(mView.context.getString(R.string.this_cannot_be_undone))
            .setPositiveButton(mView.context.getString(R.string.delete)) { _, _ ->
                deleteWorkDate(workDate)
            }.setNeutralButton(mView.context.getString(R.string.cancel), null).show()
    }

    private fun deleteWorkDate(workDate: WorkDates) {
        payDayViewModel.updateWorkDate(
            WorkDates(
                workDate.workDateId,
                workDate.wdPayPeriodId,
                workDate.wdEmployerId,
                workDate.wdCutoffDate,
                workDate.wdDate,
                workDate.wdRegHours,
                workDate.wdOtHours,
                workDate.wdDblOtHours,
                workDate.wdStatHours,
                workDate.wdNote,
                true,
                df.getCurrentTimeAsString()
            )
        )
        deleteExtras(workDate)
        deleteWorkOrders(workDate)
        timeSheetFragment.populatePayDetails()
    }

    private fun deleteWorkOrders(workDate: WorkDates) {
        workOrderViewModel.deleteWorkOrderHistoryByWorkDateId(
            workDate.workDateId, df.getCurrentTimeAsString()
        )
    }

    private fun deleteExtras(workDate: WorkDates) {
        payDayViewModel.deleteWorkDateExtrasByDateId(
            workDate.workDateId, df.getCurrentTimeAsString()
        )
    }

    private fun gotoWorkDateUpdate(workDate: WorkDates) {
        mainViewModel.apply {
            setWorkDateObject(workDate)
            setCutOffDate(curCutoff)
            setEmployer(curEmployer)
        }
        gotoWorkDateUpdateFragment()
    }

    private fun gotoWorkDateUpdateFragment() {
        mView.findNavController().navigate(
            TimeSheetFragmentDirections.actionTimeSheetFragmentToWorkDateUpdateFragment()
        )
    }
}