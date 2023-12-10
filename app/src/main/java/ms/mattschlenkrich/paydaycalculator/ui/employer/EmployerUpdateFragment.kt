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
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.INTERVAL_MONTHLY
import ms.mattschlenkrich.paydaycalculator.common.INTERVAL_SEMI_MONTHLY
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentEmployerUpdateBinding
import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.viewModel.EmployerViewModel

class EmployerUpdateFragment : Fragment(R.layout.fragment_employer_update) {

    private var _binding: FragmentEmployerUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var employerViewModel: EmployerViewModel
    private val df = DateFunctions()

    // private val cf = CommonFunctions()
    private val employerList = ArrayList<Employers>()
    private var newEmployer: Employers? = null
    private var startDate = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployerUpdateBinding.inflate(inflater, container, false)
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.update_this_employer)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        employerViewModel = mainActivity.employerViewModel
        getEmployerList()
        fillSpinners()
        fillMenu()
        setDateAction()
        setSpinnerActions()
        fillValues()
    }

    private fun fillValues() {
        if (mainActivity.mainViewModel.getEmployer() != null) {
            newEmployer = mainActivity.mainViewModel.getEmployer()!!
            binding.apply {
                etName.setText(newEmployer!!.employerName)
                for (i in 0 until spFrequency.adapter.count) {
                    if (spFrequency.getItemAtPosition(i) == newEmployer!!.payFrequency) {
                        spFrequency.setSelection(i)
                        break
                    }
                }
                startDate = newEmployer!!.startDate
                tvStartDate.text = df.getDisplayDate(startDate)
                for (i in 0 until spDayOfWeek.adapter.count) {
                    if (spDayOfWeek.getItemAtPosition(i) == newEmployer!!.dayOfWeek) {
                        spDayOfWeek.setSelection(i)
                        break
                    }
                }
                etDaysBefore.setText(newEmployer!!.cutoffDaysBefore.toString())
                etMidMonthDate.setText(newEmployer!!.midMonthlyDate.toString())
                etMainMonthDate.setText(newEmployer!!.mainMonthlyDay.toString())
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
                        updateEmployer()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun updateEmployer() {
        binding.apply {
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
                newEmployer!!.employerId,
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
            } else if (nameFound) {
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
                resources.getStringArray(R.array.pay_frequencies)
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
        employerViewModel.getCurrentEmployers().observe(
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