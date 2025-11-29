package ms.mattschlenkrich.paycalculator.ui.payrate

import android.app.DatePickerDialog
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
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_EMPLOYER_UPDATE
import ms.mattschlenkrich.paycalculator.common.FRAG_PAY_RATES
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.PayRateBasedOn
import ms.mattschlenkrich.paycalculator.database.model.employer.EmployerPayRates
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.viewModel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentEmployerPayRateAddBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import java.time.LocalDate

class EmployerPayRateAddFragment : Fragment(R.layout.fragment_employer_pay_rate_add) {

    private var _binding: FragmentEmployerPayRateAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var employerViewModel: EmployerViewModel
    private val df = DateFunctions()
    private val cf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployerPayRateAddBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        employerViewModel = mainActivity.employerViewModel
        mainActivity.topMenuBar.title = getString(R.string.add_a_pay_rate)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        binding.apply {
            tvEffectiveDate.text = LocalDate.now().minusMonths(1).toString()
            changeEffectiveDate()
        }
        populateSpinner()
    }

    private fun populateSpinner() {
        val frequencyAdapter = ArrayAdapter(
            mView.context,
            R.layout.spinner_item_bold,
            PayRateBasedOn.entries
        )
        frequencyAdapter.setDropDownViewResource(R.layout.spinner_item_bold)
        binding.spPerFrequency.adapter = frequencyAdapter
    }

    private fun setClickActions() {
        binding.apply {
            tvEffectiveDate.setOnClickListener {
                changeEffectiveDate()
            }
        }
        setMenuActions()
    }

    private fun changeEffectiveDate() {
        binding.apply {
            val curDateAll = tvEffectiveDate.text.toString().split("-")
            val datePickerDialog = DatePickerDialog(
                requireContext(), { _, year, monthOfYear, dayOfMonth ->
                    val month = monthOfYear + 1
                    val display = "$year-${
                        month.toString().padStart(2, '0')
                    }-${
                        dayOfMonth.toString().padStart(2, '0')
                    }"
                    tvEffectiveDate.text = display
                }, curDateAll[0].toInt(), curDateAll[1].toInt() - 1, curDateAll[2].toInt()
            )
            datePickerDialog.setTitle(
                getString(R.string.choose_when_this_wage_goes_into_effect)
            )
            datePickerDialog.show()
        }
    }

    private fun setMenuActions() {
        mainActivity.topMenuBar.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.save_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_save -> {
                        savePayRate()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun savePayRate() {
        val curEmployer = mainViewModel.getEmployer()!!
        val message = validatePayRate()
        if (message == ANSWER_OK) {
            val curWage = getCurrentPayRates(curEmployer)
            employerViewModel.insertPayRate(curWage)
            gotoCallingFragment()
        } else {
            displayMessage(getString(R.string.error_) + message)
        }
    }

    private fun displayMessage(message: String) {
        Toast.makeText(
            mView.context, message, Toast.LENGTH_LONG
        ).show()
    }

    private fun validatePayRate(): String {
        binding.apply {
            return if (etWage.text.isNullOrBlank()) {
                getString(R.string.there_has_to_be_a_wage_to_save)
            } else {
                ANSWER_OK
            }
        }
    }

    private fun getCurrentPayRates(
        curEmployer: Employers
    ): EmployerPayRates {
        binding.apply {
            return EmployerPayRates(
                cf.generateRandomIdAsLong(),
                curEmployer.employerId,
                tvEffectiveDate.text.toString(),
                spPerFrequency.selectedItemPosition,
                cf.getDoubleFromDollars(etWage.text.toString()),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun gotoCallingFragment() {
        val callingFragment = mainViewModel.getCallingFragment()!!
        if (callingFragment.contains(FRAG_PAY_RATES)) {
            gotoPayRateFragment()
        } else if (callingFragment.contains(FRAG_EMPLOYER_UPDATE)) {
            gotoEmployerUpdateFragment()
        }
    }

    private fun gotoPayRateFragment() {
        mView.findNavController().navigate(
            EmployerPayRateAddFragmentDirections.actionEmployerPayRateAddFragmentToEmployerPayRatesFragment()
        )
    }

    private fun gotoEmployerUpdateFragment() {
        mView.findNavController().navigate(
            EmployerPayRateAddFragmentDirections.actionEmployerPayRateAddFragmentToEmployerUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}