package ms.mattschlenkrich.paycalculator.ui.employer

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.INTERVAL_MONTHLY
import ms.mattschlenkrich.paycalculator.common.INTERVAL_SEMI_MONTHLY
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.PayDayFrequencies
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.common.WorkDayOfWeek
import ms.mattschlenkrich.paycalculator.database.model.employer.EmployerTaxTypes
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.viewModel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkTaxViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentEmployerUpdateBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity

private const val TAG = "EmployerAddFragment"

class EmployerAddFragment : Fragment(R.layout.fragment_employer_update) {

    private var _binding: FragmentEmployerUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var employerViewModel: EmployerViewModel
    private lateinit var workTaxViewModel: WorkTaxViewModel
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private lateinit var employerList: List<Employers>
    private var startDate = df.getCurrentDateAsString()

    private val mainScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployerUpdateBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        employerViewModel = mainActivity.employerViewModel
        workTaxViewModel = mainActivity.workTaxViewModel
        mainViewModel = mainActivity.mainViewModel
        mainActivity.topMenuBar.title = getString(R.string.add_an_employer)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInitialValues()
        setClickActions()
    }

    private fun setInitialValues() {
        mainScope.launch {
            populateEmployerListForValidation()
            populateSpinners()
            delay(WAIT_250)
            populateCheckStartDate()
            hideUnusedElements()
            populateDefaultValues()
        }
    }

    private fun hideUnusedElements() {
        binding.apply {
            crdTaxes.visibility = View.GONE
            crdExtras.visibility = View.GONE
            lblWage.visibility = View.GONE
            btnWage.visibility = View.GONE
            fabDone.visibility = View.GONE
            lblMidMonthDate.visibility = View.GONE
            etMidMonthDate.visibility = View.GONE
            lblMainMonthDate.visibility = View.GONE
            etMainMonthDate.visibility = View.GONE
        }
    }

    private fun populateDefaultValues() {
        binding.apply {
            etDaysBefore.setText(getString(R.string._6))
            etMidMonthDate.setText(getString(R.string._15))
            etMainMonthDate.setText(getString(R.string._31))
        }
    }

    private fun populateEmployerListForValidation() {
        employerList = employerViewModel.getEmployerList()
    }

    private fun populateSpinners() {
        binding.apply {
            val frequencyAdapter = ArrayAdapter(
                mView.context,
                R.layout.spinner_item_bold,
                PayDayFrequencies.toArray()

            )
            frequencyAdapter.setDropDownViewResource(R.layout.spinner_item_bold)
            spFrequency.adapter = frequencyAdapter
            val dayOfWeekAdapter = ArrayAdapter(
                mView.context,
                R.layout.spinner_item_bold,
                WorkDayOfWeek.toArray()
            )
            dayOfWeekAdapter.setDropDownViewResource(R.layout.spinner_item_bold)
            spDayOfWeek.adapter = dayOfWeekAdapter
        }
    }

    private fun populateCheckStartDate() {
        startDate = df.getCurrentDateAsString()
        binding.tvStartDate.text = df.getDisplayDate(startDate)
    }

    private fun setClickActions() {
        binding.apply {
            crdTaxes.setOnClickListener {
                setTaxOptions()
            }
            crdExtras.setOnClickListener {
                setExtraOptions()
            }
            tvStartDate.setOnClickListener {
                changeCheckDate()
            }
        }
        setMenuActions()
        setSpinnerActions()
    }

    private fun setTaxOptions() {
        AlertDialog.Builder(mView.context).setMessage(
            getString(R.string.you_cannot_add_taxes_until_the_employer_is_saved)
        ).setNegativeButton(getString(R.string.ok), null).show()
    }

    private fun setExtraOptions() {
        AlertDialog.Builder(mView.context).setMessage(
            getString(R.string.you_cannot_add_any_extra_credits_or_deductions_until_the_employer_is_saved)
        ).setNegativeButton(getString(R.string.ok), null).show()
    }

    private fun changeCheckDate() {
        val curDateAll = startDate.split("-")
        val datePickerDialog = DatePickerDialog(
            requireContext(), { _, year, monthOfYear, dayOfMonth ->
                val month = monthOfYear + 1
                val display = "$year-${
                    month.toString().padStart(2, '0')
                }-${
                    dayOfMonth.toString().padStart(2, '0')
                }"
                startDate = display
                binding.tvStartDate.text = df.getDisplayDate(startDate)
            }, curDateAll[0].toInt(), curDateAll[1].toInt() - 1, curDateAll[2].toInt()
        )
        datePickerDialog.setTitle(
            getString(R.string.choose_the_first_date)
        )
        datePickerDialog.show()
    }

    private fun setMenuActions() {
        mainActivity.topMenuBar.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.save_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_save -> {
                        saveEmployerAndContinue()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun setSpinnerActions() {
        binding.apply {
            spFrequency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    changeUiVisibilities()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    //not needed
                }
            }
        }
    }

    private fun changeUiVisibilities() {
        binding.apply {
            when (spFrequency.selectedItem.toString()) {
                INTERVAL_SEMI_MONTHLY -> {
                    lblMidMonthDate.visibility = View.VISIBLE
                    etMidMonthDate.visibility = View.VISIBLE
                    lblMainMonthDate.visibility = View.VISIBLE
                    etMainMonthDate.visibility = View.VISIBLE
                }

                INTERVAL_MONTHLY -> {
                    lblMidMonthDate.visibility = View.GONE
                    etMidMonthDate.visibility = View.GONE
                    lblMainMonthDate.visibility = View.VISIBLE
                    etMainMonthDate.visibility = View.VISIBLE
                }

                else -> {
                    lblMidMonthDate.visibility = View.GONE
                    etMidMonthDate.visibility = View.GONE
                    lblMainMonthDate.visibility = View.GONE
                    etMainMonthDate.visibility = View.GONE
                }
            }
        }
    }

    private fun saveEmployerAndContinue() {
        binding.apply {
            val message = validateEmployer()
            if (message == ANSWER_OK) {
                val curEmployer = getCurrentEmployer()
                employerViewModel.employerLogicViewModel.currentEmployerObj.setEmployer(curEmployer)
                employerViewModel.employerLogicViewModel.previousEmployerObj.setEmployer(curEmployer)
                displayMessage(curEmployer.toString())
//                confirmSaveAndContinue(curEmployer)
            } else {
                displayMessage(getString(R.string.error_) + message)
            }
        }
    }

    private fun saveEmployer(curEmployer: Employers) {
        employerViewModel.insertEmployer(
            curEmployer
        )
    }

    private fun displayMessage(message: String) {
        Toast.makeText(mView.context, message, Toast.LENGTH_LONG).show()
    }

    private fun validateEmployer(): String {
        binding.apply {
            if (etName.text.isNullOrBlank()) {
                return getString(R.string.the_employer_must_have_a_name)
            }
            if (employerList.isNotEmpty()) {
                for (employer in employerList) {
                    if (employer.employerName == etName.text.toString().trim()) {
                        return getString(R.string.this_employer_already_exists)
                    }
                }
            }
            if (etDaysBefore.text.isNullOrBlank()) {
                return getString(R.string.the_number_of_days_before_the_pay_day_is_required)
            }
            if (etMidMonthDate.text.isNullOrBlank()) {
                return getString(R.string.for_semi_monthly_pay_days_there_needs_to_be_a_mid_month_pay_day)
            }
            return ANSWER_OK
        }
    }

    private fun addEmployerTaxRules(employerId: Long) {
        workTaxViewModel.getTaxTypes().observe(
            viewLifecycleOwner
        ) { type ->
            type.forEach {
                mainActivity.workTaxViewModel.insertEmployerTaxType(
                    EmployerTaxTypes(
                        etrEmployerId = employerId,
                        etrTaxType = it.taxType,
                        etrInclude = true,
                        etrIsDeleted = false,
                        etrUpdateTime = df.getCurrentTimeAsString()
                    )
                )
            }
        }
    }

    private fun getCurrentEmployer(): Employers {
//        return employerObject.getEmployer()
        binding.apply {
            return Employers(
                nf.generateRandomIdAsLong(),
                etName.text.toString(),
                spFrequency.selectedItem.toString(),
                startDate,
                spDayOfWeek.selectedItem.toString(),
                etDaysBefore.text.toString().toInt(),
                etMidMonthDate.text.toString().toInt(),
                etMainMonthDate.text.toString().toInt(),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun confirmSaveAndContinue(curEmployer: Employers) {
        AlertDialog.Builder(mView.context).setTitle(
            getString(R.string.choose_next_steps_for) + curEmployer.employerName
        ).setMessage(
            getString(R.string.would_you_like_to_go_to_the_next_step)
        ).setPositiveButton(getString(R.string.yes)) { _, _ ->
            mainScope.launch {
                saveEmployer(curEmployer)
                delay(WAIT_250)
                addEmployerTaxRules(curEmployer.employerId)
                delay(WAIT_250)
                gotoEmployerUpdate(curEmployer)
            }
        }.setNegativeButton(getString(R.string.go_back), null).show()
    }

    private fun gotoEmployerUpdate(curEmployer: Employers) {
        mainViewModel.setEmployer(curEmployer)
        gotoEmployerUpdateFragment()
    }

    private fun gotoEmployerUpdateFragment() {
        mView.findNavController().navigate(
            EmployerAddFragmentDirections.actionEmployerAddFragmentToEmployerUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}