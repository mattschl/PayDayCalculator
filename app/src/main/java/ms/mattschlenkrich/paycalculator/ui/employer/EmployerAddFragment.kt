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
import ms.mattschlenkrich.paycalculator.database.model.employer.EmployerObj
import ms.mattschlenkrich.paycalculator.database.model.employer.EmployerTaxTypes
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.viewModel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkTaxViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentEmployerUpdateBinding
import ms.mattschlenkrich.paycalculator.logic.employer.EmployerLogicViewModel
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
    private lateinit var employerObj: EmployerObj
    private lateinit var employerLogic: EmployerLogicViewModel
    private val df = DateFunctions()
    private val nf = NumberFunctions()
//    private lateinit var employerList: List<Employers>
//    private var startDate = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployerUpdateBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        employerViewModel = mainActivity.employerViewModel
        employerObj = employerViewModel.employerLogicViewModel.currentEmployerObj
        employerLogic = employerViewModel.employerLogicViewModel
        workTaxViewModel = mainActivity.workTaxViewModel
        mainViewModel = mainActivity.mainViewModel
        mainActivity.title = getString(R.string.add_an_employer)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInitialValues()
        setClickActions()
    }

    private fun setInitialValues() {
        hideUiElements()
//        populateEmployerListForValidation()
        populateSpinners()
        populateCheckStartDate()
        populateDefaultValues()
    }

    private fun populateDefaultValues() {
        binding.apply {
            etDaysBefore.setText("6")
            etMidMonthDate.setText("15")
            etMainMonthDate.setText("31")
        }

    }

    private fun hideUiElements() {
        binding.apply {
            lblWage.visibility = View.GONE
            btnWage.visibility = View.GONE
            crdTaxes.visibility = View.GONE
            crdExtras.visibility = View.GONE
            fabDone.visibility = View.GONE
        }
    }

//    private fun populateEmployerListForValidation() {
//        employerViewModel.getEmployers().observe(viewLifecycleOwner) { employers ->
//            employerList = employers
//        }
//    }

    private fun populateSpinners() {
        binding.apply {
            val frequencyAdapter = ArrayAdapter(
                mView.context,
                R.layout.spinner_item_bold,
                PayDayFrequencies.entries
            )
            spFrequency.adapter = frequencyAdapter

            val dayOfWeekAdapter = ArrayAdapter(
                mView.context,
                R.layout.spinner_item_bold,
                WorkDayOfWeek.entries
            )
            dayOfWeekAdapter.setDropDownViewResource(R.layout.spinner_item_bold)
            spDayOfWeek.adapter = dayOfWeekAdapter
        }
    }

    private fun populateCheckStartDate() {
        employerObj.startDate = df.getCurrentDateAsString()
        binding.tvStartDate.text = employerObj.startDate
    }

    private fun setClickActions() {
        binding.apply {
//            crdTaxes.setOnClickListener {
//                setTaxOptions()
//            }
//            crdExtras.setOnClickListener {
//                setExtraOptions()
//            }
            tvStartDate.setOnClickListener {
                changeCheckDate()
            }
        }
        setMenuActions()
        setSpinnerActions()
    }

//    private fun setTaxOptions() {
//        AlertDialog.Builder(mView.context).setMessage(
//            getString(R.string.you_cannot_add_taxes_until_the_employer_is_saved)
//        ).setNegativeButton(getString(R.string.ok), null).show()
//    }
//
//    private fun setExtraOptions() {
//        AlertDialog.Builder(mView.context).setMessage(
//            getString(R.string.you_cannot_add_any_extra_credits_or_deductions_until_the_employer_is_saved)
//        ).setNegativeButton(getString(R.string.ok), null).show()
//    }

    private fun changeCheckDate() {
        val curDateAll = employerObj.startDate.split("-")
        val datePickerDialog = DatePickerDialog(
            requireContext(), { _, year, monthOfYear, dayOfMonth ->
                val month = monthOfYear + 1
                val display = "$year-${
                    month.toString().padStart(2, '0')
                }-${
                    dayOfMonth.toString().padStart(2, '0')
                }"
                employerObj.startDate = display
                binding.tvStartDate.text = df.getDisplayDate(employerObj.startDate)
            }, curDateAll[0].toInt(), curDateAll[1].toInt() - 1, curDateAll[2].toInt()
        )
        datePickerDialog.setTitle(
            getString(R.string.choose_the_first_date)
        )
        datePickerDialog.show()
    }

    private fun setMenuActions() {
        mainActivity.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.save_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_save -> {
                        transferData()
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

    private fun transferData() {
        binding.apply {
            employerObj.employerName = etName.text.toString()
            employerObj.payFrequency = spFrequency.selectedItem.toString()
            employerObj.startDate = tvStartDate.text.toString()
            employerObj.dayOfWeek = spDayOfWeek.selectedItem.toString()
            employerObj.cutoffDaysBefore = etDaysBefore.text.toString()
            employerObj.midMonthlyDate = etMidMonthDate.text.toString()
            employerObj.mainMonthlyDate = etMainMonthDate.text.toString()
            employerObj.employerIsDeleted = false
            employerObj.employerUpdateTime = df.getCurrentTimeAsString()
        }
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
            val message = employerLogic.validateEmployer()
            if (message == ANSWER_OK) {
                val curEmployer = getCurrentEmployer()
                chooseToSaveAndContinue(curEmployer)
            } else {
                displayMessage(getString(R.string.error_) + message)
            }
        }
    }

    private fun saveEmployer() {
        employerViewModel.employerLogicViewModel.addEmployer()
    }

    private fun displayMessage(message: String) {
        Toast.makeText(mView.context, message, Toast.LENGTH_LONG).show()
    }

//    private fun validateEmployer(): String {
//        binding.apply {
//            if (etName.text.isNullOrBlank()) {
//                return getString(R.string.the_employer_must_have_a_name)
//            }
//            if (employerList.isNotEmpty()) {
//                for (employer in employerList) {
//                    if (employer.employerName == etName.text.toString().trim()) {
//                        return getString(R.string.this_employer_already_exists)
//                    }
//                }
//            }
//            if (etDaysBefore.text.isNullOrBlank()) {
//                return getString(R.string.the_number_of_days_before_the_pay_day_is_required)
//            }
//            if (etMidMonthDate.text.isNullOrBlank()) {
//                return getString(R.string.for_semi_monthly_pay_days_there_needs_to_be_a_mid_month_pay_day)
//            }
//            return ANSWER_OK
//        }
//    }

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
        return employerObj.getEmployer()
//        binding.apply {
//            return Employers(
//                nf.generateRandomIdAsLong(),
//                etName.text.toString(),
//                spFrequency.selectedItem.toString(),
//                employerObj.startDate,
//                spDayOfWeek.selectedItem.toString(),
//                etDaysBefore.text.toString().toInt(),
//                etMidMonthDate.text.toString().toInt(),
//                etMainMonthDate.text.toString().toInt(),
//                false,
//                df.getCurrentTimeAsString()
//            )
//        }
    }

    private val mainScope = CoroutineScope(Dispatchers.Main)

    private fun chooseToSaveAndContinue(curEmployer: Employers) {
        AlertDialog.Builder(mView.context).setTitle(
            getString(R.string.choose_next_steps_for) + curEmployer.employerName
        ).setMessage(
            getString(R.string.would_you_like_to_go_to_the_next_step)
        ).setPositiveButton(getString(R.string.yes)) { _, _ ->
            mainScope.launch {
                saveEmployer()
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