package ms.mattschlenkrich.paydaycalculator.ui.employer

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
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.FRAG_EMPLOYERS
import ms.mattschlenkrich.paydaycalculator.common.FRAG_TIME_SHEET
import ms.mattschlenkrich.paydaycalculator.common.FRAG_WORK_ORDERS
import ms.mattschlenkrich.paydaycalculator.common.INTERVAL_MONTHLY
import ms.mattschlenkrich.paydaycalculator.common.INTERVAL_SEMI_MONTHLY
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.database.model.employer.EmployerTaxTypes
import ms.mattschlenkrich.paydaycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentEmployerAddBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

//private const val TAG = "EmployerAddFragment"

class EmployerAddFragment : Fragment(R.layout.fragment_employer_add) {

    private var _binding: FragmentEmployerAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private val employerList = ArrayList<Employers>()
    private var startDate = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployerAddBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.add_an_employer)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInitialValues()
        setClickActions()
    }

    private fun setInitialValues() {
        populateEmployerListForValidation()
        populateSpinners()
        populateCheckStartDate()
    }

    private fun populateEmployerListForValidation() {
        mainActivity.employerViewModel.getEmployers()
            .observe(viewLifecycleOwner) { employers ->
                employerList.clear()
                employers.listIterator().forEach {
                    employerList.add(it)
                }
            }
    }

    private fun populateSpinners() {
        binding.apply {
            val frequencyAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_bold,
                resources.getStringArray(R.array.pay_day_frequencies)
            )
            frequencyAdapter.setDropDownViewResource(R.layout.spinner_item_bold)
            spFrequency.adapter = frequencyAdapter

            val dayOfWeekAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_bold,
                resources.getStringArray(R.array.pay_days)
            )
            dayOfWeekAdapter.setDropDownViewResource(R.layout.spinner_item_bold)
            spDayOfWeek.adapter = dayOfWeekAdapter
        }
    }

    private fun populateCheckStartDate() {
        startDate = df.getCurrentDateAsString()
        binding.tvStartDate.text = df.getDisplayDate(startDate)
    }

    private fun setMenuActions() {
        mainActivity.addMenuProvider(object : MenuProvider {
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

    private fun changeCheckDate() {
        val curDateAll = startDate.split("-")
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
                startDate = display
                binding.tvStartDate.text = df.getDisplayDate(startDate)
            },
            curDateAll[0].toInt(),
            curDateAll[1].toInt() - 1,
            curDateAll[2].toInt()
        )
        datePickerDialog.setTitle(
            getString(R.string.choose_the_first_date)
        )
        datePickerDialog.show()
    }

    private fun setSpinnerActions() {
        binding.apply {
            spFrequency.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        changeUiVisibilities()
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        //not needed
                    }
                }
        }
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

    private fun setExtraOptions() {
        AlertDialog.Builder(mView.context)
            .setMessage(
                getString(R.string.you_cannot_add_any_extra_credits_or_deductions_until_the_employer_is_saved)
            )
            .setNegativeButton(getString(R.string.ok), null)
            .show()
    }

    private fun setTaxOptions() {
        AlertDialog.Builder(mView.context)
            .setMessage(
                getString(R.string.you_cannot_add_taxes_until_the_employer_is_saved)
            )
            .setNegativeButton(getString(R.string.ok), null)
            .show()
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
                mainActivity.employerViewModel.insertEmployer(
                    curEmployer
                )
                addEmployerTaxRules(curEmployer.employerId)
                chooseNextStepsAfterSaving(curEmployer)
            } else {
                Toast.makeText(
                    mView.context,
                    message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun validateEmployer(): String {
        binding.apply {
            if (etName.text.isNullOrBlank()) {
                return getString(R.string.error_) +
                        getString(R.string.the_employer_must_have_a_name)
            }
            if (employerList.isNotEmpty()) {
                for (employer in employerList) {
                    if (employer.employerName == etName.text.toString().trim()) {
                        return getString(R.string.error_) +
                                getString(R.string.this_employer_already_exists)
                    }
                }
            }
            if (etDaysBefore.text.isNullOrBlank()) {
                return getString(R.string.error_) +
                        getString(R.string.the_number_of_days_before_the_pay_day_is_required)
            }
            if (etMidMonthDate.text.isNullOrBlank()) {
                return getString(R.string.error_) +
                        getString(R.string.for_semi_monthly_pay_days_there_needs_to_be_a_mid_month_pay_day)
            }
            return ANSWER_OK
        }
    }

    private fun addEmployerTaxRules(employerId: Long) {
        mainActivity.workTaxViewModel.getTaxTypes().observe(
            viewLifecycleOwner
        ) { type ->
            type.forEach {
                mainActivity.workTaxViewModel.insertEmployerTaxType(
                    EmployerTaxTypes(
                        etrEmployerId = employerId,
                        etrTaxType = it.taxType,
                        etrInclude = false,
                        etrIsDeleted = false,
                        etrUpdateTime = df.getCurrentTimeAsString()
                    )
                )
            }
        }
    }

    private fun getCurrentEmployer(): Employers {
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

    private fun chooseNextStepsAfterSaving(curEmployer: Employers) {
        AlertDialog.Builder(mView.context)
            .setTitle(
                getString(R.string.choose_next_steps_for) +
                        curEmployer.employerName
            )
            .setMessage(
                getString(R.string.the_taxes_have_been_set_up_automatically)
            )
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                gotoEmployerExtrasFragment(curEmployer)
            }
            .setNegativeButton(getString(R.string.no)) { _, _ ->
                gotoCallingFragment(curEmployer)
            }
            .show()
    }

    private fun gotoCallingFragment(employer: Employers) {
        when (mainActivity.mainViewModel.getCallingFragment()) {
            FRAG_EMPLOYERS -> {
                gotoEmployerFragment()
            }

            FRAG_WORK_ORDERS -> {
                gotoWorkOrdersFragment(employer)
            }

            FRAG_TIME_SHEET -> {
                gotoTimeSheetFragment(employer)
            }
        }
    }

    private fun gotoTimeSheetFragment(employer: Employers) {
        mainActivity.mainViewModel.setEmployer(employer)
        mView.findNavController().navigate(
            EmployerAddFragmentDirections
                .actionEmployerAddFragmentToTimeSheetFragment()
        )
    }

    private fun gotoWorkOrdersFragment(employer: Employers) {
        mainActivity.mainViewModel.setEmployer(employer)
        mView.findNavController().navigate(
            EmployerAddFragmentDirections
                .actionEmployerAddFragmentToWorkOrdersFragment()
        )
    }

    private fun gotoEmployerExtrasFragment(curEmployer: Employers) {
        mainActivity.mainViewModel.setEmployer(curEmployer)
        mView.findNavController().navigate(
            EmployerAddFragmentDirections
                .actionEmployerAddFragmentToEmployerUpdateFragment()
        )
    }

    private fun gotoEmployerFragment() {
        mainActivity.mainViewModel.setEmployer(null)
        mView.findNavController().navigate(
            EmployerAddFragmentDirections
                .actionEmployerAddFragmentToEmployerFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}