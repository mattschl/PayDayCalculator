package ms.mattschlenkrich.paycalculator.ui.extras.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_PAY_DETAILS
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkPayPeriodExtras
import ms.mattschlenkrich.paycalculator.databinding.ListPayDetailExtraItemBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.paydays.IPayDetailsFragment
import ms.mattschlenkrich.paycalculator.ui.paydays.PayDetailFragmentDirections

//private const val TAG = "PayDetailExtraAdapter"

class PayDetailExtraAdapter(
    private val mainActivity: MainActivity,
    private val creditList: ArrayList<WorkPayPeriodExtras>,
    private val mView: View,
    private val parentFragment: IPayDetailsFragment
) : RecyclerView.Adapter<PayDetailExtraAdapter.CreditViewHolder>() {

    private val cf = NumberFunctions()
    private val df = DateFunctions()

    class CreditViewHolder(val itemBinding: ListPayDetailExtraItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreditViewHolder {
        return CreditViewHolder(
            ListPayDetailExtraItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return creditList.size
    }

    override fun onBindViewHolder(holder: CreditViewHolder, position: Int) {
        val extra = creditList[position]
        holder.itemBinding.apply {
            tvExtraDescription.text = extra.ppeName
            if (!extra.ppeIsDeleted) {
                chActive.isChecked = true
                btnEdit.visibility = View.VISIBLE
                tvExtraTotal.text = cf.displayDollars(
                    extra.ppeValue
                )
            } else {
                chActive.isChecked = false
                btnEdit.visibility = View.INVISIBLE
                tvExtraTotal.visibility = View.INVISIBLE
            }
            if (extra.ppeAttachTo == 0 || extra.ppeAttachTo == 1) {
                btnEdit.visibility = View.INVISIBLE
                chActive.visibility = View.INVISIBLE
            } else {
                btnEdit.visibility = View.VISIBLE
                chActive.visibility = View.VISIBLE
            }
            btnEdit.setOnClickListener {
                confirmUpdateExtra(extra, !chActive.isChecked)
            }
            chActive.setOnClickListener {
                insertOrUpdateExtra(extra, !chActive.isChecked)
                CoroutineScope(Dispatchers.Main).launch {
                    delay(WAIT_250)
                    parentFragment.populatePayDetails()
                }
            }
        }
    }


    private fun confirmUpdateExtra(extra: WorkPayPeriodExtras, delete: Boolean) {
        AlertDialog.Builder(mView.context)
            .setTitle(mView.context.getString(R.string.continue_to_update))
            .setMessage(
                mView.context.getString(R.string.if_this_is_edited_any_default_calculations_will_be_overwritten)
            )
            .setPositiveButton(mView.context.getString(R.string.yes)) { _, _ ->
                val newExtra = insertOrUpdateExtra(extra, delete)
                updateExtra(newExtra)
            }
            .setNegativeButton(mView.context.getString(R.string.cancel), null)
            .show()

    }

    private fun updateExtra(newExtra: WorkPayPeriodExtras) {
        mainActivity.mainViewModel.setPayPeriodExtra(newExtra)
        mainActivity.mainViewModel.addCallingFragment(FRAG_PAY_DETAILS)
        gotoPeriodExtraUpdateFragment()
    }

    private fun insertOrUpdateExtra(
        extra: WorkPayPeriodExtras, delete: Boolean
    ): WorkPayPeriodExtras {
        var notFound = true
        mainActivity.payDayViewModel.findPayPeriodExtra(
            extra.ppeName
        ).observe(mView.findViewTreeLifecycleOwner()!!) { found ->
            if (found == null) notFound = false
        }
        val newExtra = WorkPayPeriodExtras(
            extra.workPayPeriodExtraId,
            extra.ppePayPeriodId,
            extra.ppeExtraTypeId,
            extra.ppeName,
            extra.ppeAppliesTo,
            3,
            extra.ppeValue,
            extra.ppeIsFixed,
            extra.ppeIsCredit,
            delete,
            df.getCurrentTimeAsString()
        )
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_250)
            if (notFound) {
                mainActivity.payDayViewModel.insertPayPeriodExtra(
                    newExtra
                )
            } else {
                mainActivity.payDayViewModel.updatePayPeriodExtra(
                    newExtra
                )
            }
        }
        return newExtra
    }

    private fun gotoPeriodExtraUpdateFragment() {
        mView.findNavController().navigate(
            PayDetailFragmentDirections
                .actionPayDetailsFragmentToPayPeriodExtraUpdateFragment()
        )
    }

}