package ms.mattschlenkrich.paydaycalculator.adapter

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
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.FRAG_PAY_DETAILS
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.common.WAIT_250
import ms.mattschlenkrich.paydaycalculator.common.WAIT_500
import ms.mattschlenkrich.paydaycalculator.databinding.ListPayDetailExtraItemBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkPayPeriodExtras
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import ms.mattschlenkrich.paydaycalculator.ui.paydays.PayDetailsFragment
import ms.mattschlenkrich.paydaycalculator.ui.paydays.PayDetailsFragmentDirections

//private const val TAG = "PayDetailExtraAdapter"

class PayDetailExtraAdapter(
    private val mainActivity: MainActivity,
    private val creditList: ArrayList<WorkPayPeriodExtras>,
    private val mView: View,
    private val parentFragment: PayDetailsFragment
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
                gotoUpdateExtra(extra, !chActive.isChecked)
            }
            chActive.setOnClickListener {
                insertOrUpdateExtra(extra, !chActive.isChecked)
                CoroutineScope(Dispatchers.Main).launch {
                    delay(WAIT_500)
                    parentFragment.fillPayDetails()
                }
            }
        }
    }


    private fun gotoUpdateExtra(extra: WorkPayPeriodExtras, delete: Boolean) {
        AlertDialog.Builder(mView.context)
            .setTitle("Continue to update?")
            .setMessage(
                "If this is edited, any custom calculations will be overwritten. " +
                        "Would you like to edit it anyways?"
            )
            .setPositiveButton("Yes") { _, _ ->
                val newExtra = insertOrUpdateExtra(extra, delete)
                updateExtra(newExtra)
            }
            .setNegativeButton("Cancel", null)
            .show()

    }

    private fun updateExtra(newExtra: WorkPayPeriodExtras) {
        mainActivity.mainViewModel.setPayPeriodExtra(newExtra)
        mainActivity.mainViewModel.addCallingFragment(FRAG_PAY_DETAILS)
        mView.findNavController().navigate(
            PayDetailsFragmentDirections
                .actionPayDetailsFragmentToPayPeriodExtraUpdateFragment()
        )
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
}