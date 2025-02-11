package ms.mattschlenkrich.paycalculator.ui.employer

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
import ms.mattschlenkrich.paycalculator.common.FRAG_PAY_RATES
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.database.model.employer.EmployerPayRates
import ms.mattschlenkrich.paycalculator.databinding.FragmentEmployerWageUpdateBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity

//private const val TAG = FRAG_PAY_RATE_UPDATE

class EmployerPayRateUpdateFragment : Fragment(R.layout.fragment_employer_wage_update) {

    private var _binding: FragmentEmployerWageUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var curPayRate: EmployerPayRates
    private val df = DateFunctions()
    private val cf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployerWageUpdateBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        val display =
            getString(R.string.edit_pay_rate_for) +
                    mainActivity.mainViewModel.getEmployer()!!.employerName
        mainActivity.title = display
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        populateSpinner()
        binding.apply {
            curPayRate = mainActivity.mainViewModel.getPayRate()!!
            tvEffectiveDate.text = curPayRate.eprEffectiveDate
            etWage.setText(cf.displayDollars(curPayRate.eprPayRate))
            spPerFrequency.setSelection(curPayRate.eprPerPeriod)
        }
    }

    private fun populateSpinner() {
        val frequencyAdapter = ArrayAdapter(
            mView.context, R.layout.spinner_item_bold,
            resources.getStringArray(R.array.pay_day_frequencies)
        )
        frequencyAdapter.setDropDownViewResource(R.layout.spinner_item_bold)
        binding.spPerFrequency.adapter = frequencyAdapter
    }

    private fun setClickActions() {
        setMenuActions()
        binding.apply {
            fabDone.setOnClickListener {
                updatePayRateIfValid()
            }

            tvEffectiveDate.setOnClickListener {
                changeDate()
            }
        }
    }

    private fun setMenuActions() {
        mainActivity.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_delete, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_delete -> {
                        deletePayRate()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun updatePayRateIfValid() {
        binding.apply {
            val message = validatePayRate()
            if (message == ANSWER_OK) {
                updatePayRate()
                gotoCallingFragment()
            } else {
                displayError(message)
            }
        }
    }

    private fun displayError(message: String) {
        Toast.makeText(
            mView.context,
            getString(R.string.error_) + message,
            Toast.LENGTH_LONG
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

    private fun updatePayRate() {
        binding.apply {
            mainActivity.employerViewModel.updatePayRate(
                EmployerPayRates(
                    curPayRate.employerPayRateId,
                    curPayRate.eprEmployerId,
                    tvEffectiveDate.text.toString(),
                    spPerFrequency.selectedItemPosition,
                    cf.getDoubleFromDollars(etWage.text.toString()),
                    false,
                    df.getCurrentTimeAsString()
                )
            )
        }
    }

    private fun deletePayRate() {
        mainActivity.employerViewModel.updatePayRate(
            EmployerPayRates(
                curPayRate.employerPayRateId,
                curPayRate.eprEmployerId,
                curPayRate.eprEffectiveDate,
                curPayRate.eprPerPeriod,
                curPayRate.eprPayRate,
                true,
                df.getCurrentTimeAsString()
            )
        )
        gotoCallingFragment()
    }

    private fun changeDate() {
        binding.apply {
            val curDateAll = tvEffectiveDate.text.toString().split("-")
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, monthOfYear, dayOfMonth ->
                    val month = monthOfYear + 1
                    val display = "$year-${
                        month.toString()
                            .padStart(2, '0')
                    }-${
                        dayOfMonth.toString().padStart(2, '0')
                    }"
                    tvEffectiveDate.text = display
                },
                curDateAll[0].toInt(),
                curDateAll[1].toInt() - 1,
                curDateAll[2].toInt()
            )
            datePickerDialog.setTitle(
                getString(R.string.choose_when_this_wage_goes_into_effect)
            )
            datePickerDialog.show()
        }
    }

    private fun gotoCallingFragment() {
        if (mainActivity.mainViewModel.getCallingFragment()!!.contains(FRAG_PAY_RATES)) {
            gotoPayRateFragment()
        }
    }

    private fun gotoPayRateFragment() {
        mView.findNavController().navigate(
            EmployerPayRateUpdateFragmentDirections
                .actionEmployerWageUpdateFragmentToEmployerPayRatesFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}