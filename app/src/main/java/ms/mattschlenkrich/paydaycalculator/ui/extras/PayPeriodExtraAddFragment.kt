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
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.CommonFunctions
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentPayPeriodExtraAddBinding
import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.model.PayPeriods
import ms.mattschlenkrich.paydaycalculator.model.WorkPayPeriodExtras

class PayPeriodExtraAddFragment :
    Fragment(R.layout.fragment_pay_period_extra_add) {

    private var _binding: FragmentPayPeriodExtraAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var curPayPeriod: PayPeriods
    private lateinit var curEmployer: Employers
    private var extraList = ArrayList<WorkPayPeriodExtras>()
    private val df = DateFunctions()
    private val cf = CommonFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPayPeriodExtraAddBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = "Add an extra to this pay period"
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fillSpinners()
        chooseFixedOrPercent()
        fillMenu()
        fillValues()
    }

    private fun fillValues() {
        if (mainActivity.mainViewModel.getEmployer() != null) {
            curEmployer = mainActivity.mainViewModel.getEmployer()!!
        }
        if (mainActivity.mainViewModel.getPayPeriod() != null) {
            curPayPeriod = mainActivity.mainViewModel.getPayPeriod()!!
        }
        val display = "Cutoff Date: ${curPayPeriod.ppCutoffDate} for : " +
                curEmployer.employerName
        binding.apply {
            lblPayInfo.text = display
            chkIsCredit.isChecked = mainActivity.mainViewModel.getIsCredit()
        }
    }

    private fun fillMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.save_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_save -> {
                        saveExtra()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun saveExtra() {
        val message = checkExtra()
        if (message == ANSWER_OK) {
            binding.apply {
                mainActivity.payDayViewModel.insertPayPeriodExtra(
                    curPayPeriodExtra()
                )
            }
            gotoPayDetails()
        } else {
            Toast.makeText(
                mView.context,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun gotoPayDetails() {
        mView.findNavController().navigate(
            PayPeriodExtraAddFragmentDirections
                .actionPayPeriodExtraAddFragmentToPayDetailsFragment()
        )
    }

    private fun curPayPeriodExtra(): WorkPayPeriodExtras {
        binding.apply {
            return WorkPayPeriodExtras(
                cf.generateId(),
                curPayPeriod.payPeriodId,
                null,
                etExtraName.text.toString().trim(),
                spAppliesTo.selectedItemPosition,
                3,
                cf.getDoubleFromDollarOrPercent(
                    etValue.text.toString()
                ),
                chkIsFixed.isChecked,
                chkIsCredit.isChecked,
                false,
                df.getCurrentTimeAsString(),
            )
        }
    }

    private fun checkExtra(): String {
        binding.apply {
            var nameFound = false
            if (extraList.isNotEmpty()) {
                for (extra in extraList) {
                    if (extra.ppeName == etExtraName.text.toString().trim()) {
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