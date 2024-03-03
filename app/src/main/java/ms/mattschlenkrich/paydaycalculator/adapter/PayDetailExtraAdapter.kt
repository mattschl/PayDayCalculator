package ms.mattschlenkrich.paydaycalculator.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.common.CommonFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.ListPayDetailExtraItemBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkPayPeriodExtras
import ms.mattschlenkrich.paydaycalculator.payFunctions.PayCalculations
import ms.mattschlenkrich.paydaycalculator.ui.paydays.PayDetailsFragment

class PayDetailExtraAdapter(
    private val creditList: ArrayList<WorkPayPeriodExtras>,
    private val mView: View,
    private val payCalculations: PayCalculations,
    private val parentFragment: PayDetailsFragment
) : RecyclerView.Adapter<PayDetailExtraAdapter.CreditViewHolder>() {

    private val cf = CommonFunctions()

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
            btnEdit.setOnClickListener {
                gotoUpdateExtra(extra)
            }
            chActive.setOnClickListener {
                insertOrUpdateExtra(extra, chActive.isChecked)
            }
        }
    }

    private fun gotoUpdateExtra(extraFull: WorkPayPeriodExtras) {
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
        Toast.makeText(
            mView.context,
            "This function is not available",
            Toast.LENGTH_LONG
        ).show()
        parentFragment.fillPayDetails()
    }
}