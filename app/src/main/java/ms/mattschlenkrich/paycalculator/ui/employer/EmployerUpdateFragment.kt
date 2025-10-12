package ms.mattschlenkrich.paycalculator.ui.employer

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
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_EMPLOYER_UPDATE
import ms.mattschlenkrich.paycalculator.common.INTERVAL_MONTHLY
import ms.mattschlenkrich.paycalculator.common.INTERVAL_SEMI_MONTHLY
import ms.mattschlenkrich.paycalculator.common.WAIT_500
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.viewModel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkTaxViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentEmployerUpdateBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.employer.adapter.EmployerExtraDefinitionsShortAdapter
import ms.mattschlenkrich.paycalculator.ui.employer.adapter.EmployerTaxTypeAdapter

private const val TAG = FRAG_EMPLOYER_UPDATE

class EmployerUpdateFragment : Fragment(R.layout.fragment_employer_update),
    IEmployerUpdateFragment {

    private var _binding: FragmentEmployerUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var employerViewModel: EmployerViewModel
    private lateinit var workTaxViewModel: WorkTaxViewModel
    private val df = DateFunctions()
    private lateinit var employerList: List<Employers>
    private var curEmployer: Employers? = null
    private var startDate = ""
    private val mainScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployerUpdateBinding.inflate(inflater, container, false)
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        employerViewModel = mainActivity.employerViewModel
        workTaxViewModel = mainActivity.workTaxViewModel
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        populateEmployerListForValidation()
        populateSpinners()
        populateUIValues()
    }

    private fun populateEmployerListForValidation() {
        employerViewModel.getEmployers().observe(
            viewLifecycleOwner
        ) { employers ->
            employerList = employers
        }
    }

    private fun populateSpinners() {
        binding.apply {
            val frequencyAdapter = ArrayAdapter(
                mView.context,
                R.layout.spinner_item_bold,
                resources.getStringArray(R.array.pay_day_frequencies)
            )
            frequencyAdapter.setDropDownViewResource(R.layout.spinner_item_bold)
            spFrequency.adapter = frequencyAdapter

            val dayOfWeekAdapter = ArrayAdapter(
                mView.context,
                R.layout.spinner_item_bold,
                resources.getStringArray(R.array.pay_days)
            )
            dayOfWeekAdapter.setDropDownViewResource(R.layout.spinner_item_bold)
            spDayOfWeek.adapter = dayOfWeekAdapter
        }
    }


    private fun populateUIValues() {
        mainScope.launch {
            delay(WAIT_500)
            if (mainViewModel.getEmployer() != null) {
                mainScope.launch {
                    curEmployer = mainActivity.mainViewModel.getEmployer()!!
                    ifPayRateNotExistsGotoPayRate(curEmployer!!.employerId)
                    binding.apply {
                        mainActivity.title =
                            getString(R.string.update) + " " + curEmployer!!.employerName
                        etName.setText(curEmployer!!.employerName)
                        for (i in 0 until spFrequency.adapter.count) {
                            if (spFrequency.getItemAtPosition(i) == curEmployer!!.payFrequency) {
                                spFrequency.setSelection(i)
                                break
                            }
                        }
                        startDate = curEmployer!!.startDate
                        tvStartDate.text = df.getDisplayDate(startDate)
                        for (i in 0 until spDayOfWeek.adapter.count) {
                            if (spDayOfWeek.getItemAtPosition(i) == curEmployer!!.dayOfWeek) {
                                spDayOfWeek.setSelection(i)
                                break
                            }
                        }
                        etDaysBefore.setText(
                            String.format(curEmployer!!.cutoffDaysBefore.toString())
                        )
                        etMidMonthDate.setText(
                            String.format(curEmployer!!.midMonthlyDate.toString())
                        )
                        etMainMonthDate.setText(
                            String.format(curEmployer!!.mainMonthlyDate.toString())
                        )
                    }
                    populateTaxes(curEmployer!!.employerId)
                    populateExtras(curEmployer!!.employerId)
                }
            }
        }
    }

    private fun ifPayRateNotExistsGotoPayRate(employerId: Long) {
        employerViewModel.getEmployerPayRates(employerId).observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                gotoPayRateAdd(curEmployer!!)
            }
        }

    }

    override fun populateTaxes(employerId: Long) {
        binding.apply {
            val employerTaxTypeAdapter = EmployerTaxTypeAdapter(
                mainActivity, this@EmployerUpdateFragment
            )
            rvTaxes.apply {
                layoutManager = LinearLayoutManager(mView.context)
                adapter = employerTaxTypeAdapter
            }
            workTaxViewModel.getEmployerTaxTypes(
                employerId
            ).observe(viewLifecycleOwner) { employerTaxType ->
                employerTaxTypeAdapter.differ.submitList(employerTaxType)
            }
        }
    }

    override fun populateExtras(employerId: Long) {
        binding.apply {
            val extraTypeAdapter = EmployerExtraDefinitionsShortAdapter(
                curEmployer!!, mainActivity, this@EmployerUpdateFragment,
            )
            rvExtras.apply {
                layoutManager = LinearLayoutManager(mView.context)
                adapter = extraTypeAdapter
            }
            activity.let {
                mainActivity.workExtraViewModel.getWorkExtraTypeList(
                    employerId
                ).observe(viewLifecycleOwner) { list ->
                    extraTypeAdapter.differ.submitList(list)

                }
            }
        }
    }

    private fun setClickActions() {
        setMenuActions()
        binding.apply {
            fabDone.setOnClickListener { updateEmployerIfValid() }
            fabAddTax.setOnClickListener { gotoTaxTypesAdd() }
            lblTaxes.setOnLongClickListener {
                gotoTaxRules()
                false
            }
            fabAddExtra.setOnClickListener { gotoExtraAdd(curEmployer) }
            btnWage.setOnClickListener { gotoPayRate(curEmployer) }
            binding.tvStartDate.setOnClickListener { changeCheckDate() }
            setSpinnerActions()
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
                        deleteEmployer()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun updateEmployerIfValid() {
        val message = validateEmployer()
        if (message == ANSWER_OK) {
            updateEmployer()
            gotoEmployerFragment()
        } else {
            displayMessage(getString(R.string.error_) + message)
        }
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
                    if (employer.employerName == etName.text.toString()
                            .trim() && employer.employerName != curEmployer!!.employerName
                    ) {
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

    private fun getCurrentEmployer(): Employers {
        binding.apply {
            return Employers(
                curEmployer!!.employerId,
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

    private fun updateEmployer() {
        employerViewModel.updateEmployer(
            getCurrentEmployer()
        )
    }

    private fun setSpinnerActions() {
        binding.apply {
            spFrequency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
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

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    //not needed
                }
            }
        }
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
        datePickerDialog.setTitle(getString(R.string.choose_first_check_date))
        datePickerDialog.show()
    }

    private fun deleteEmployer() {
        binding.apply {
            employerViewModel.updateEmployer(
                Employers(
                    curEmployer!!.employerId,
                    etName.text.toString(),
                    spFrequency.selectedItem.toString(),
                    startDate,
                    spDayOfWeek.selectedItem.toString(),
                    etDaysBefore.text.toString().toInt(),
                    etMidMonthDate.text.toString().toInt(),
                    etMainMonthDate.text.toString().toInt(),
                    true,
                    df.getCurrentTimeAsString()
                )
            )
        }
        gotoEmployerFragment()
    }

    private fun gotoEmployerFragment() {
        mView.findNavController().navigate(
            EmployerUpdateFragmentDirections.actionEmployerUpdateFragmentToEmployerFragment()
        )
    }

    private fun gotoTaxRules() {
        mainViewModel.apply {
            setEmployer(getCurrentEmployer())
            setCallingFragment(TAG)
        }
        gotoTaxRulesFragment()
    }

    private fun gotoTaxRulesFragment() {
        mView.findNavController().navigate(
            EmployerUpdateFragmentDirections.actionEmployerUpdateFragmentToTaxRulesFragment()
        )
    }

    private fun gotoTaxTypesAdd() {
        mainViewModel.apply {
            setEmployer(getCurrentEmployer())
            setCallingFragment(TAG)
        }
        gotoTaxTypesAddFragment()
    }

    private fun gotoTaxTypesAddFragment() {
        mView.findNavController().navigate(
            EmployerUpdateFragmentDirections.actionEmployerUpdateFragmentToTaxTypeAddFragment()
        )
    }

    private fun gotoPayRate(curEmployer: Employers?) {
        if (curEmployer != null) {
            mainViewModel.setEmployer(curEmployer)
            gotoEmployerPayRatesFragment()
        }
    }

    private fun gotoEmployerPayRatesFragment() {
        mView.findNavController().navigate(
            EmployerUpdateFragmentDirections.actionEmployerUpdateFragmentToEmployerPayRatesFragment()
        )
    }

    private fun gotoExtraAdd(curEmployer: Employers?) {
        if (curEmployer != null) {
            mainViewModel.setEmployer(curEmployer)
            gotoEmployerExtraDefinitionsAddFragment()
        }
    }

    private fun gotoEmployerExtraDefinitionsAddFragment() {
        mView.findNavController().navigate(
            EmployerUpdateFragmentDirections.actionEmployerUpdateFragmentToEmployerExtraDefinitionsAddFragment()
        )
    }

    private fun gotoPayRateAdd(curEmployer: Employers) {
        mainViewModel.apply {
            setEmployer(curEmployer)
            setCallingFragment(TAG)
        }
        gotoPayRateAddFragment()
    }

    private fun gotoPayRateAddFragment() {
        mView.findNavController().navigate(
            EmployerUpdateFragmentDirections.actionEmployerUpdateFragmentToEmployerPayRateAddFragment()
        )
    }

    fun gotoEmployerExtraDefinitionsFragment() {
        mView.findNavController().navigate(
            EmployerUpdateFragmentDirections.actionEmployerUpdateFragmentToEmployerExtraDefinitionsFragment()
        )
    }

    fun gotoRulesFragment() {
        mView.findNavController().navigate(
            EmployerUpdateFragmentDirections.actionEmployerUpdateFragmentToTaxRulesFragment()
        )
    }

    override fun gotoEmployerExtraDefinitionUpdateFragment() {
        mView.findNavController().navigate(
            EmployerUpdateFragmentDirections.actionEmployerUpdateFragmentToEmployerExtraDefinitionUpdateFragment()
        )
    }

    override fun onStop() {
        mainScope.cancel()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}