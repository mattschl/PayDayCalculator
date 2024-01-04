package ms.mattschlenkrich.paydaycalculator.ui.employer

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.adapter.EmployerExtraDefinitionFullAdapter
import ms.mattschlenkrich.paydaycalculator.adapter.EmployerTaxTypeAdapter
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.FRAG_EMPLOYER_UPDATE
import ms.mattschlenkrich.paydaycalculator.common.INTERVAL_MONTHLY
import ms.mattschlenkrich.paydaycalculator.common.INTERVAL_SEMI_MONTHLY
import ms.mattschlenkrich.paydaycalculator.common.WAIT_500
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentEmployerUpdateBinding
import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.viewModel.EmployerViewModel

private const val TAG = FRAG_EMPLOYER_UPDATE

class EmployerUpdateFragment : Fragment(R.layout.fragment_employer_update) {

    private var _binding: FragmentEmployerUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var employerViewModel: EmployerViewModel
    private val df = DateFunctions()
    private var employerTaxTypeAdapter: EmployerTaxTypeAdapter? = null
    private var extraDefinitionsAdapter: EmployerExtraDefinitionFullAdapter? = null

    // private val cf = CommonFunctions()
    private val employerList = ArrayList<Employers>()
    private var curEmployer: Employers? = null
    private var startDate = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployerUpdateBinding.inflate(inflater, container, false)
        mView = binding.root
        mainActivity = (activity as MainActivity)

        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        employerViewModel = mainActivity.employerViewModel
        getEmployerList()
        fillSpinners()
        fillMenu()
        setDateAction()
        setActions()
        setSpinnerActions()
        fillValues()
    }

    private fun setActions() {
        binding.apply {
            fabDone.setOnClickListener {
                updateEmployer()
            }
            fabAddTax.setOnClickListener {
                gotoTaxTypesAdd()
            }
            lblTaxes.setOnLongClickListener {
                gotoTaxRules()
                false
            }
        }
    }

    private fun gotoTaxRules() {
        mainActivity.mainViewModel.setEmployer(getCurrentEmployer())
        mainActivity.mainViewModel.setCallingFragment(TAG)
        mView.findNavController().navigate(
            EmployerUpdateFragmentDirections
                .actionEmployerUpdateFragmentToTaxRulesFragment()
        )
    }

    private fun gotoTaxTypesAdd() {
        mainActivity.mainViewModel.setEmployer(getCurrentEmployer())
        mainActivity.mainViewModel.setCallingFragment(TAG)
        mView.findNavController().navigate(
            EmployerUpdateFragmentDirections
                .actionEmployerUpdateFragmentToTaxTypeAddFragment()
        )
    }

    private fun fillValues() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_500)
            if (mainActivity.mainViewModel.getEmployer() != null) {
                curEmployer = mainActivity.mainViewModel.getEmployer()!!
                binding.apply {
                    mainActivity.title = getString(R.string.update) +
                            curEmployer!!.employerName
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
                    etDaysBefore.setText(curEmployer!!.cutoffDaysBefore.toString())
                    etMidMonthDate.setText(curEmployer!!.midMonthlyDate.toString())
                    etMainMonthDate.setText(curEmployer!!.mainMonthlyDate.toString())
                }
                fillTaxes(curEmployer!!.employerId)
                fillExtras(curEmployer!!.employerId)
            }
        }
    }

    fun fillExtras(employerId: Long) {
        binding.apply {
            extraDefinitionsAdapter = null
            extraDefinitionsAdapter =
                EmployerExtraDefinitionFullAdapter(
                    mainActivity, mView,
                    null,
                    this@EmployerUpdateFragment
                )
            rvExtras.apply {
                layoutManager = LinearLayoutManager(mView.context)
//                    StaggeredGridLayoutManager(
//                    2,
//                    StaggeredGridLayoutManager.VERTICAL
//                )
//                setHasFixedSize(true)
                adapter = extraDefinitionsAdapter
            }
            activity.let {
                mainActivity.workExtraViewModel.getActiveExtraDefinitionsFull(
                    employerId
                ).observe(viewLifecycleOwner) { list ->
                    extraDefinitionsAdapter!!.differ.submitList(list)
                }
            }
        }
    }


    fun fillTaxes(employerId: Long) {
        binding.apply {
            employerTaxTypeAdapter = null
            employerTaxTypeAdapter = EmployerTaxTypeAdapter(
                mainActivity, mView, this@EmployerUpdateFragment
            )
            rvTaxes.apply {
                layoutManager = LinearLayoutManager(mView.context)
                adapter = employerTaxTypeAdapter
            }
            activity.let {
                mainActivity.workTaxViewModel.getEmployerTaxTypes(
                    employerId
                ).observe(viewLifecycleOwner) { employerTaxType ->
                    employerTaxTypeAdapter!!.differ.submitList(employerTaxType)
                }
            }
        }
    }

    private fun setSpinnerActions() {
        binding.apply {
            spFrequency.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
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

    private fun setDateAction() {
        binding.tvStartDate.setOnClickListener {
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
            datePickerDialog.setTitle("Choose the first date")
            datePickerDialog.show()
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
        gotoCallingFragment()
    }

    private fun updateEmployer() {
        val message = checkEmployer()
        if (message == ANSWER_OK) {
            employerViewModel.updateEmployer(
                getCurrentEmployer()
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

    private fun gotoCallingFragment() {
        mView.findNavController().navigate(
            EmployerUpdateFragmentDirections
                .actionEmployerUpdateFragmentToEmployerFragment()
        )
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

    private fun checkEmployer(): String {
        binding.apply {
            var nameFound = false
            if (employerList.isNotEmpty()) {
                for (employer in employerList) {
                    if (employer.employerName == etName.text.toString().trim()) {
                        nameFound = true
                        break
                    }
                }
            }
            val errorMessage = if (etName.text.isNullOrBlank()) {
                "    ERROR!!\n" +
                        "The employer must have a name!"
            } else if (nameFound && etName.text.toString() != curEmployer!!.employerName) {
                "    ERROR!!\n" +
                        "This employer already exists!"
            } else if (etDaysBefore.text.isNullOrBlank()) {
                "    ERROR!!\n" +
                        "A number of days before the pay day is required!"
            } else if (etMidMonthDate.text.isNullOrBlank()) {
                "    ERROR!!\n" +
                        "For semi-monthly pay days there needs to be a mid month pay day"
            } else {
                ANSWER_OK
            }
            return errorMessage
        }
    }

    private fun fillSpinners() {
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

    private fun getEmployerList() {
        employerViewModel.getEmployers().observe(
            viewLifecycleOwner
        ) { employers ->
            employerList.clear()
            employers.listIterator().forEach {
                employerList.add(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}