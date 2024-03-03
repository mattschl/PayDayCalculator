package ms.mattschlenkrich.paydaycalculator.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.common.CommonFunctions
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.ListPayDetailExtraItemBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkPayPeriodExtras
import ms.mattschlenkrich.paydaycalculator.ui.paydays.PayDetailsFragment

private const val TAG = "PayDetailExtraAdapter"

class PayDetailExtraAdapter(
    private val mainActivity: MainActivity,
    private val creditList: ArrayList<WorkPayPeriodExtras>,
    private val mView: View,
    private val parentFragment: PayDetailsFragment
) : RecyclerView.Adapter<PayDetailExtraAdapter.CreditViewHolder>() {

    private val cf = CommonFunctions()
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
                gotoUpdateExtra(extra)
            }
            chActive.setOnClickListener {
                insertOrUpdateExtra(extra, !chActive.isChecked)
                parentFragment.fillPayDetails()
            }
        }
    }

    private fun gotoUpdateExtra(extra: WorkPayPeriodExtras) {
        //todo: create the fragment and view
        Toast.makeText(
            mView.context,
            "This function is not available",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun insertOrUpdateExtra(
        extra: WorkPayPeriodExtras, delete: Boolean
    ) {
        var notFound = false
        mainActivity.payDayViewModel.findPayPeriodExtra(
            extra.workPayPeriodExtraId
        ).observe(mView.findViewTreeLifecycleOwner()!!) { found ->
            if (found == null) notFound = true
        }
        Log.d(
            TAG, "The extra notFound = $notFound and" +
                    "delete is $delete"
        )
        if (notFound) {
            mainActivity.payDayViewModel.insertPayPeriodExtra(
                WorkPayPeriodExtras(
                    extra.workPayPeriodExtraId,
                    extra.ppePayPeriodId,
                    extra.ppeExtraTypeId,
                    extra.ppeName,
                    extra.ppeAppliesTo,
                    extra.ppeAttachTo,
                    extra.ppeValue,
                    extra.ppeIsFixed,
                    extra.ppeIsCredit,
                    delete,
                    df.getCurrentTimeAsString()
                )
            )
        } else {
            Log.d(TAG, "Will update")
            mainActivity.payDayViewModel.updatePayPeriodExtra(
                WorkPayPeriodExtras(
                    extra.workPayPeriodExtraId,
                    extra.ppePayPeriodId,
                    extra.ppeExtraTypeId,
                    extra.ppeName,
                    extra.ppeAppliesTo,
                    extra.ppeAttachTo,
                    extra.ppeValue,
                    extra.ppeIsFixed,
                    extra.ppeIsCredit,
                    delete,
                    df.getCurrentTimeAsString()
                )
            )
        }
    }
}