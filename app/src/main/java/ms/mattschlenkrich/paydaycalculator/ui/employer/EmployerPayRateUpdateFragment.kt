package ms.mattschlenkrich.paydaycalculator.ui.employer

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
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.FRAG_PAY_RATES
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentEmployerWageUpdateBinding
import ms.mattschlenkrich.paydaycalculator.model.employer.EmployerPayRates
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

//private const val TAG = FRAG_PAY_RATE_UPDATE

class EmployerPayRateUpdateFragment : Fragment(R.layout.fragment_employer_wage_update) {

    private var _binding: FragmentEmployerWageUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()
    private val cf = NumberFunctions()
    private lateinit var curPayRate: EmployerPayRates

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
            "Edit pay rate for ${mainActivity.mainViewModel.getEmployer()!!.employerName}"
        mainActivity.title = display
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fillSpinner()
        fillMenu()
        setActions()
        setDateAction()
        fillValues()
    }

    private fun setActions() {
        binding.apply {
            fabDone.setOnClickListener {
                savePayRate()
            }
        }
    }

    private fun savePayRate() {
        binding.apply {
            val message = checkPayRate()
            if (message == ANSWER_OK) {
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
                gotoCallingFragment()
            } else {
                Toast.makeText(
                    mView.context,
                    message,
                    Toast.LENGTH_LONG
                ).show()
            }
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

    private fun checkPayRate(): String {
        binding.apply {
            return if (etWage.text.isNullOrBlank()) {
                "    ERROR!!\n" +
                        "There has to be a wage to save"
            } else {
                ANSWER_OK
            }
        }
    }

    private fun fillSpinner() {
        val frequencyAdapter = ArrayAdapter(
            mView.context, R.layout.spinner_item_bold,
            resources.getStringArray(R.array.pay_per_frequencies)
        )
        frequencyAdapter.setDropDownViewResource(R.layout.spinner_item_bold)
        binding.spPerFrequency.adapter = frequencyAdapter
    }

    private fun fillValues() {
        binding.apply {
            curPayRate = mainActivity.mainViewModel.getPayRate()!!
            tvEffectiveDate.text = curPayRate.eprEffectiveDate
            etWage.setText(cf.displayDollars(curPayRate.eprPayRate))
            spPerFrequency.setSelection(curPayRate.eprPerPeriod)
        }
    }

    private fun setDateAction() {
        binding.apply {
            tvEffectiveDate.setOnClickListener {
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
    }

    private fun fillMenu() {
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}