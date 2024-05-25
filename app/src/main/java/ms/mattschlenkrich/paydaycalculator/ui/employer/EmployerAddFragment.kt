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
import ms.mattschlenkrich.paydaycalculator.common.INTERVAL_MONTHLY
import ms.mattschlenkrich.paydaycalculator.common.INTERVAL_SEMI_MONTHLY
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentEmployerAddBinding
import ms.mattschlenkrich.paydaycalculator.model.employer.EmployerTaxTypes
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
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
        _binding = FragmentEmployerAddBinding.inflate(inflater, container, false)
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.add_an_employer)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getEmployerList()
        fillSpinnersAndStartDate()
        fillMenu()
        setDateAction()
        setSpinnerActions()
        setOtherActions()
    }

    private fun setOtherActions() {
        binding.apply {
            crdTaxes.setOnClickListener {
                AlertDialog.Builder(mView.context)
                    .setMessage(
                        "You cannot add taxes until the employer is saved. " +
                                "Save the employer first and go to edit mode."
                    )
                    .setNegativeButton("OK", null)
                    .show()
            }
            crdExtras.setOnClickListener {
                AlertDialog.Builder(mView.context)
                    .setMessage(
                        "You cannot add any extra credits or deductions until the employer is saved. " +
                                "Save the employer first and go to edit mode."
                    )
                    .setNegativeButton("OK", null)
                    .show()
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
                menuInflater.inflate(R.menu.save_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_save -> {
                        saveEmployer()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun saveEmployer() {
        binding.apply {
            val message = checkEmployerToSave()
            if (message == ANSWER_OK) {
                val curEmployer = getCurrentEmployer()
                mainActivity.employerViewModel.insertEmployer(
                    curEmployer
                )
                addEmployerTaxRules(curEmployer.employerId)
                chooseNextSteps(curEmployer)
            } else {
                Toast.makeText(
                    mView.context,
                    message,
                    Toast.LENGTH_LONG
                ).show()
            }
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

    private fun chooseNextSteps(curEmployer: Employers) {
        AlertDialog.Builder(mView.context)
            .setTitle("Choose next steps for ${curEmployer.employerName}")
            .setMessage(
                "The taxes have been set up automatically. \n" +
                        "Would you like to open this employer in EDIT mode to edit taxes, " +
                        "create extra deductions or extra items added to paychecks?"
            )
            .setPositiveButton("Yes") { _, _ ->
                gotoEmployerExtras(curEmployer)
            }
            .setNegativeButton("No") { _, _ ->
                gotoCallingFragment()
            }
            .show()
    }

    private fun gotoEmployerExtras(curEmployer: Employers) {
        mainActivity.mainViewModel.setEmployer(curEmployer)
        mView.findNavController().navigate(
            EmployerAddFragmentDirections
                .actionEmployerAddFragmentToEmployerUpdateFragment()
        )
    }

    private fun gotoCallingFragment() {
        mView.findNavController().navigate(
            EmployerAddFragmentDirections
                .actionEmployerAddFragmentToEmployerFragment()
        )
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

    private fun checkEmployerToSave(): String {
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
            } else if (nameFound) {
                "    ERROR!!\n" +
                        "This employer already exists!"
            } else if (etDaysBefore.text.isNullOrBlank()) {
                "    ERROR!!\n" +
                        "The number of days before the pay day is required!"
            } else if (etMidMonthDate.text.isNullOrBlank()) {
                "    ERROR!!\n" +
                        "For semi-monthly pay days there needs to be a mid month pay day"
            } else {
                ANSWER_OK
            }
            return errorMessage
        }

    }

    private fun fillSpinnersAndStartDate() {
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
            startDate = df.getCurrentDateAsString()
            tvStartDate.text = df.getDisplayDate(startDate)
        }
    }

    private fun getEmployerList() {
        mainActivity.employerViewModel.getEmployers().observe(viewLifecycleOwner) { employers ->
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