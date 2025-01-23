package ms.mattschlenkrich.paycalculator.ui.paydays.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import ms.mattschlenkrich.paycalculator.common.WAIT_500
import ms.mattschlenkrich.paycalculator.database.model.extras.ExtraContainer
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkPayPeriodExtras
import ms.mattschlenkrich.paycalculator.databinding.ListPayDetailExtraItemBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.paydays.IPayDetailsFragment
import ms.mattschlenkrich.paycalculator.ui.paydays.PayDetailFragmentDirections

private const val TAG = "PayDetailExtraContainerAdapter"

class PayDetailExtraContainerAdapter(
    private val mainActivity: MainActivity,
    private val extraContainerList: List<ExtraContainer>,
    private val mView: View,
    private val parentFragment: IPayDetailsFragment
) : RecyclerView.Adapter<PayDetailExtraContainerAdapter.ExtraViewHolder>() {

    private val cf = NumberFunctions()
    private val df = DateFunctions()

    class ExtraViewHolder(val itemBinding: ListPayDetailExtraItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExtraViewHolder {
        return ExtraViewHolder(
            ListPayDetailExtraItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return extraContainerList.size
    }

    override fun onBindViewHolder(holder: ExtraViewHolder, position: Int) {
        val extraContainer = extraContainerList[position]
        holder.itemBinding.apply {
            tvExtraDescription.text = extraContainer.extraName
            chActive.isChecked = false
            btnEdit.visibility = View.INVISIBLE
            tvExtraTotal.visibility = View.INVISIBLE
            if ((extraContainer.workDateExtra != null &&
                        !extraContainer.workDateExtra!!.wdeIsDeleted) ||
                (extraContainer.payPeriodExtra != null &&
                        !extraContainer.payPeriodExtra!!.ppeIsDeleted) ||
                (extraContainer.extraDefinitionAndType != null &&
                        !extraContainer.extraDefinitionAndType!!.extraType.wetIsDeleted)
            ) {
                chActive.isChecked = true
                btnEdit.visibility = View.VISIBLE
                tvExtraTotal.text = cf.displayDollars(
                    extraContainer.amount
                )
            }
            btnEdit.setOnClickListener {
                confirmUpdateExtra(extraContainer, !chActive.isChecked)
            }
            chActive.setOnClickListener {
                insertOrUpdateExtra(extraContainer, !chActive.isChecked)
                CoroutineScope(Dispatchers.Main).launch {
                    delay(WAIT_500)
                    parentFragment.populatePayDetails()
                }
            }
        }
    }


    private fun confirmUpdateExtra(extraContainer: ExtraContainer, delete: Boolean) {
        AlertDialog.Builder(mView.context)
            .setTitle(mView.context.getString(R.string.continue_to_update))
            .setMessage(
                mView.context.getString(R.string.if_this_is_edited_any_custom_calculations_will_be_overwritten)
            )
            .setPositiveButton(mView.context.getString(R.string.yes)) { _, _ ->
                val newExtra = insertOrUpdateExtra(extraContainer, delete)
                updateExtra(newExtra)
            }
            .setNegativeButton(mView.context.getString(R.string.cancel), null)
            .show()

    }

    private fun updateExtra(extraContainer: ExtraContainer) {
        mainActivity.mainViewModel.setPayPeriodExtra(extraContainer)
        mainActivity.mainViewModel.addCallingFragment(FRAG_PAY_DETAILS)
        gotoPeriodExtraUpdateFragment()
    }

    private fun insertOrUpdateExtra(
        extraContainer: ExtraContainer, delete: Boolean
    ): ExtraContainer {
        if (extraContainer.payPeriodExtra != null) {
            mainActivity.mainViewModel.setPayPeriodExtra(
                extraContainer.payPeriodExtra!!
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
        }
        val newExtra = WorkPayPeriodExtras(
            extraContainer.payPeriodExtra.workPayPeriodExtraId,
            extraContainer.ppePayPeriodId,
            extraContainer.ppeExtraTypeId,
            extraContainer.ppeName,
            extraContainer.ppeAppliesTo,
            3,
            extraContainer.ppeValue,
            extraContainer.ppeIsFixed,
            extraContainer.ppeIsCredit,
            delete,
            df.getCurrentTimeAsString()
        )

        return newExtra
    }


    private fun gotoPeriodExtraUpdateFragment() {
        mView.findNavController().navigate(
            PayDetailFragmentDirections
                .actionPayDetailsFragmentToPayPeriodExtraUpdateFragment()
        )
    }

}