package ms.mattschlenkrich.paydaycalculator.ui.extras

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.MoneyFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentPayPeriodExtraUpdateBinding
import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.model.PayPeriods
import ms.mattschlenkrich.paydaycalculator.model.WorkPayPeriodExtras
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity


class PayPeriodExtraUpdateFragment : Fragment(R.layout.fragment_pay_period_extra_update) {

    private var _binding: FragmentPayPeriodExtraUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var curPayPeriod: PayPeriods
    private lateinit var curEmployer: Employers
    private lateinit var oldPayPeriodExtra: WorkPayPeriodExtras
    private var extraList = ArrayList<WorkPayPeriodExtras>()
    private val df = DateFunctions()
    private val cf = MoneyFunctions()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPayPeriodExtraUpdateBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = "Update extra for this pay period"
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fillSpinners()
        setActions()
        chooseFixedOrPercent()
        fillMenu()
        fillValues()
    }

    private fun fillValues() {
        extraList = mainActivity.mainViewModel.getPayPeriodExtraList()
        if (mainActivity.mainViewModel.getEmployer() != null) {
            curEmployer = mainActivity.mainViewModel.getEmployer()!!
        }
        if (mainActivity.mainViewModel.getPayPeriod() != null) {
            curPayPeriod = mainActivity.mainViewModel.getPayPeriod()!!
        }
        if (mainActivity.mainViewModel.getPayPeriodExtra() != null) {
            oldPayPeriodExtra = mainActivity.mainViewModel.getPayPeriodExtra()!!
            binding.apply {
                mainActivity.title = "Update extra: ${oldPayPeriodExtra.ppeName}"
                var display = "Pay Cutoff: ${curPayPeriod.ppCutoffDate} " +
                        "Employer: ${curEmployer.employerName}"
                lblPayInfo.text = display
                etExtraName.setText(oldPayPeriodExtra.ppeName)
                spAppliesTo.setSelection(oldPayPeriodExtra.ppeAppliesTo)
                display = if (oldPayPeriodExtra.ppeIsFixed) {
                    cf.displayDollars(oldPayPeriodExtra.ppeValue)
                } else {
                    cf.displayPercentFromDouble(oldPayPeriodExtra.ppeValue)
                }
                etValue.setText(display)
                chkIsFixed.isChecked = oldPayPeriodExtra.ppeIsFixed
                chkIsCredit.isChecked = oldPayPeriodExtra.ppeIsCredit
            }
        }
    }

    private fun setActions() {
        binding.apply {
            fabDone.setOnClickListener {
                updatePayPeriodExtra()
            }
        }
    }

    private fun updatePayPeriodExtra() {
        val message = checkExtra()
        if (message == ANSWER_OK) {
            mainActivity.payDayViewModel.updatePayPeriodExtra(
                getCurPayPeriodExtra()
            )
            gotoPayDetailFragments()
        } else {
            Toast.makeText(
                mView.context,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun gotoPayDetailFragments() {
        mainActivity.mainViewModel.clearPayPeriodExtraList()
        mainActivity.mainViewModel.setPayPeriodExtra(null)
        mView.findNavController().navigate(
            PayPeriodExtraUpdateFragmentDirections
                .actionPayPeriodExtraUpdateFragmentToPayDetailsFragment()
        )
    }

    private fun getCurPayPeriodExtra(): WorkPayPeriodExtras {
        binding.apply {
            return WorkPayPeriodExtras(
                oldPayPeriodExtra.workPayPeriodExtraId,
                oldPayPeriodExtra.ppePayPeriodId,
                oldPayPeriodExtra.ppeExtraTypeId,
                etExtraName.text.toString().trim(),
                spAppliesTo.selectedItemPosition,
                3,
                cf.getDoubleFromDollarOrPercent(
                    etValue.text.toString()
                ),
                chkIsFixed.isChecked,
                chkIsCredit.isChecked,
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun checkExtra(): String {
        binding.apply {
            var nameFound = false
            if (extraList.isNotEmpty()) {
                for (extra in extraList) {
                    if (extra.ppeName == etExtraName.text.toString().trim() &&
                        etExtraName.text.toString().trim() != oldPayPeriodExtra.ppeName
                    ) {
                        nameFound = true
                        break
                    }
                }
            }
            val errorMessage = if (etExtraName.text.isNullOrBlank()) {
                "    ERROR!!\n" +
                        "The Extra must have a name"
            } else if (nameFound) {
                "   ERROR!!\n" +
                        "This Extra name has already been used. \n" +
                        "Choose a different name."
            } else if (cf.getDoubleFromDollarOrPercent(etValue.text.toString()) == 0.0) {
                "   ERROR!!\n" +
                        "This Extra must have a value"
            } else {
                ANSWER_OK
            }
            return errorMessage
        }
    }

    private fun fillMenu() {
        mainActivity.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_delete, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_delete -> {
                        deleteExtra()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun deleteExtra() {
        Toast.makeText(
            mView.context,
            "This function is not available",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun chooseFixedOrPercent() {
        binding.apply {
            chkIsFixed.setOnClickListener {
                etValue.setText(
                    if (chkIsFixed.isChecked) {
                        cf.displayDollars(
                            cf.getDoubleFromDollarOrPercent(
                                etValue.text.toString()
                            )
                        )
                    } else {
                        cf.displayPercentFromDouble(
                            cf.getDoubleFromDollarOrPercent(
                                etValue.text.toString()
                            ) / 100
                        )
                    }
                )
            }
        }
    }

    private fun fillSpinners() {
        binding.apply {
            val frequencyAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_bold,
                resources.getStringArray(R.array.pay_per_frequencies)
            )
            frequencyAdapter.setDropDownViewResource(R.layout.spinner_item_bold)
            spAppliesTo.adapter = frequencyAdapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}