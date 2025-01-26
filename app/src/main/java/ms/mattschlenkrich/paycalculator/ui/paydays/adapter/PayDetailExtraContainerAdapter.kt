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
import ms.mattschlenkrich.paycalculator.common.WAIT_500
import ms.mattschlenkrich.paycalculator.database.model.extras.ExtraContainer
import ms.mattschlenkrich.paycalculator.database.model.payperiod.PayPeriods
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkPayPeriodExtras
import ms.mattschlenkrich.paycalculator.databinding.ListPayDetailExtraItemBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.paydays.IPayDetailsFragment
import ms.mattschlenkrich.paycalculator.ui.paydays.PayDetailFragmentNewDirections

private const val TAG = "PayDetailExtraContainerAdapter"

class PayDetailExtraContainerAdapter(
    private val mainActivity: MainActivity,
    private val payPeriod: PayPeriods,
    private val extraContainerList: List<ExtraContainer>,
    private val mView: View,
    private val parentFragment: IPayDetailsFragment
) : RecyclerView.Adapter<PayDetailExtraContainerAdapter.ExtraViewHolder>() {

    private val nf = NumberFunctions()
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
            btnEdit.visibility = View.VISIBLE
            chActive.visibility = View.VISIBLE
            tvExtraTotal.text = nf.displayDollars(
                extraContainer.amount
            )
            if (extraContainer.payPeriodExtra != null) {
                chActive.isChecked = !extraContainer.payPeriodExtra!!.ppeIsDeleted
                if (extraContainer.payPeriodExtra!!.ppeIsDeleted) {
                    btnEdit.visibility = View.INVISIBLE
                    tvExtraTotal.visibility = View.INVISIBLE
                }
            }
            if (extraContainer.workDateExtra != null) {
                chActive.isChecked = !extraContainer.workDateExtra!!.wdeIsDeleted
                if (extraContainer.workDateExtra!!.wdeIsDeleted) {
                    btnEdit.visibility = View.INVISIBLE
                    tvExtraTotal.visibility = View.INVISIBLE
                }
            }
            if (extraContainer.extraDefinitionAndType != null) {
                chActive.isChecked =
                    !extraContainer.extraDefinitionAndType!!.extraType.wetIsDeleted &&
                            !extraContainer.extraDefinitionAndType!!.definition.weIsDeleted
                if (extraContainer.extraDefinitionAndType!!.extraType.wetIsDeleted ||
                    extraContainer.extraDefinitionAndType!!.definition.weIsDeleted
                ) {
                    btnEdit.visibility = View.INVISIBLE
                    tvExtraTotal.visibility = View.INVISIBLE
                }
            }
            btnEdit.setOnClickListener {
                confirmUpdateExtra(extraContainer, !chActive.isChecked)
            }
            chActive.setOnClickListener {
                insertOrUpdateExtraOnChange(extraContainer, !chActive.isChecked)
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
                mView.context.getString(R.string.if_this_is_edited_any_default_calculations_will_be_overwritten)
            )
            .setPositiveButton(mView.context.getString(R.string.yes)) { _, _ ->
                insertOrUpdateExtraOnChange(extraContainer, delete)
                updateExtra(extraContainer)
            }
            .setNegativeButton(mView.context.getString(R.string.cancel), null)
            .show()

    }

    private fun updateExtra(extraContainer: ExtraContainer) {
        mainActivity.mainViewModel.setPayPeriodExtra(extraContainer.payPeriodExtra)
        mainActivity.mainViewModel.setWorkDateExtra(null)
        mainActivity.mainViewModel.setExtraContainer(extraContainer)
        mainActivity.mainViewModel.addCallingFragment(FRAG_PAY_DETAILS)
        gotoPeriodExtraUpdateFragment()
    }

    private fun insertOrUpdateExtraOnChange(
        extraContainer: ExtraContainer, delete: Boolean
    ): ExtraContainer {
        CoroutineScope(Dispatchers.Default).launch {
            if (extraContainer.payPeriodExtra != null) {
                val newExtra = WorkPayPeriodExtras(
                    extraContainer.payPeriodExtra!!.workPayPeriodExtraId,
                    extraContainer.payPeriodExtra!!.ppePayPeriodId,
                    extraContainer.payPeriodExtra!!.ppeExtraTypeId,
                    extraContainer.payPeriodExtra!!.ppeName,
                    extraContainer.payPeriodExtra!!.ppeAppliesTo,
                    3,
                    extraContainer.payPeriodExtra!!.ppeValue,
                    extraContainer.payPeriodExtra!!.ppeIsFixed,
                    extraContainer.payPeriodExtra!!.ppeIsCredit,
                    delete,
                    df.getCurrentTimeAsString()
                )
                extraContainer.payPeriodExtra = newExtra
                mainActivity.payDayViewModel.updatePayPeriodExtra(newExtra)
            } else if (extraContainer.extraDefinitionAndType != null) {
                val newExtra = WorkPayPeriodExtras(
                    nf.generateRandomIdAsLong(),
                    payPeriod.payPeriodId,
                    extraContainer.extraDefinitionAndType!!.extraType.workExtraTypeId,
                    extraContainer.extraDefinitionAndType!!.extraType.wetName,
                    extraContainer.extraDefinitionAndType!!.extraType.wetAppliesTo,
                    extraContainer.extraDefinitionAndType!!.extraType.wetAttachTo,
                    extraContainer.extraDefinitionAndType!!.definition.weValue,
                    extraContainer.extraDefinitionAndType!!.definition.weIsFixed,
                    extraContainer.extraDefinitionAndType!!.extraType.wetIsCredit,
                    delete,
                    df.getCurrentTimeAsString()
                )
                extraContainer.payPeriodExtra = newExtra
                mainActivity.payDayViewModel.insertPayPeriodExtra(newExtra)
            }

        }
        return extraContainer
    }


    private fun gotoPeriodExtraUpdateFragment() {
        mView.findNavController().navigate(
            PayDetailFragmentNewDirections
                .actionPayDetailFragmentNewToPayPeriodExtraUpdateFragment()
        )
    }

}