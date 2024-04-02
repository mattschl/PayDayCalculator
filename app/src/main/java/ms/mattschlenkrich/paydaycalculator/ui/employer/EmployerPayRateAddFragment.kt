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
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentEmployerPayRateAddBinding
import ms.mattschlenkrich.paydaycalculator.model.EmployerPayRates
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import java.time.LocalDate

class EmployerPayRateAddFragment :
    Fragment(R.layout.fragment_employer_pay_rate_add) {

    private var _binding: FragmentEmployerPayRateAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()
    private val cf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployerPayRateAddBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        val display =
            "Add a pay rate"
        mainActivity.title = display
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fillMenu()
        fillSpinner()
        setDateAction()
        fillValues()
    }

    private fun fillValues() {
        binding.apply {
            tvEffectiveDate.text = LocalDate.now().toString()
            chooseDate()
        }
    }

    private fun setDateAction() {
        binding.apply {
            tvEffectiveDate.setOnClickListener {
                chooseDate()
            }
        }
    }

    private fun chooseDate() {
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

    private fun fillSpinner() {
        val frequencyAdapter = ArrayAdapter(
            mView.context, R.layout.spinner_item_bold,
            resources.getStringArray(R.array.pay_per_frequencies)
        )
        frequencyAdapter.setDropDownViewResource(R.layout.spinner_item_bold)
        binding.spPerFrequency.adapter = frequencyAdapter
    }

    private fun fillMenu() {
        mainActivity.addMenuProvider(object : MenuProvider {
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
        binding.apply {
            val curEmployer = mainActivity.mainViewModel.getEmployer()!!
            val message = checkPayRate()
            if (message == ANSWER_OK) {
                val curWage = EmployerPayRates(
                    cf.generateId(),
                    curEmployer.employerId,
                    tvEffectiveDate.text.toString(),
                    spPerFrequency.selectedItemPosition,
                    cf.getDoubleFromDollars(etWage.text.toString()),
                    false,
                    df.getCurrentTimeAsString()
                )
                mainActivity.employerViewModel.insertPayRate(curWage)
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
            EmployerPayRateAddFragmentDirections
                .actionEmployerPayRateAddFragmentToEmployerPayRatesFragment()
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}