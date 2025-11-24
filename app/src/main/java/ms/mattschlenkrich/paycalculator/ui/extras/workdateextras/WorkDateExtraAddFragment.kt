package ms.mattschlenkrich.paycalculator.ui.extras.workdateextras

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
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDateExtras
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paycalculator.database.viewModel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkDateExtraAddBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity

class WorkDateExtraAddFragment : Fragment(R.layout.fragment_work_date_extra_add) {

    private var _binding: FragmentWorkDateExtraAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var employerViewModel: EmployerViewModel
    private lateinit var payDayViewModel: PayDayViewModel
    private lateinit var curDateObject: WorkDates
    private lateinit var existingWorkDateExtraList: List<WorkDateExtras>
    private val df = DateFunctions()
    private val cf = NumberFunctions()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkDateExtraAddBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        employerViewModel = mainActivity.employerViewModel
        payDayViewModel = mainActivity.payDayViewModel
        mainActivity.topMenuBar.title = getString(R.string.add_a_one_time_extra)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        populateSpinners()
        if (mainViewModel.getWorkDateObject() != null) {
            curDateObject = mainViewModel.getWorkDateObject()!!
            employerViewModel.getEmployer(curDateObject.wdEmployerId)
                .observe(viewLifecycleOwner) { employer ->
                    val display =
                        getString(R.string.date_) + df.getDisplayDate(curDateObject.wdDate) + getString(
                            R.string.employer_
                        ) + employer.employerName
                    binding.lblDateInfo.text = display
                }
            getExtraListForDate()
        }
    }

    private fun populateSpinners() {
        binding.apply {
            val frequencies = ArrayList<String>()
            for (i in 0..1) {
                frequencies.add(
                    resources.getStringArray(R.array.attach_to_frequencies)[i]
                )
            }
            val frequencyAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_bold, frequencies
            )
            frequencyAdapter.setDropDownViewResource(R.layout.spinner_item_bold)
            spAppliesTo.adapter = frequencyAdapter
        }
    }

    private fun getExtraListForDate() {
        payDayViewModel.getWorkDateExtras(curDateObject.workDateId)
            .observe(viewLifecycleOwner) { list ->
                existingWorkDateExtraList = list
            }
    }

    private fun setClickActions() {
        setMenuActions()
        binding.chkIsFixed.setOnClickListener {
            changeTextToFixedOrPercentString()
        }
    }

    private fun setMenuActions() {
        mainActivity.topMenuBar.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.save_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_save -> {
                        saveWorkDateExtraIfValid()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun changeTextToFixedOrPercentString() {
        binding.apply {
            etValue.setText(
                if (chkIsFixed.isChecked) {
                    cf.displayDollars(
                        cf.getDoubleFromDollarOrPercentString(
                            etValue.text.toString()
                        )
                    )
                } else {
                    cf.getPercentStringFromDouble(
                        cf.getDoubleFromDollarOrPercentString(
                            etValue.text.toString()
                        ) / 100
                    )
                }
            )
        }
    }

    private fun saveWorkDateExtraIfValid() {
        val message = validateExtraForErrors()
        if (message == ANSWER_OK) {
            saveWorkDateExtra()
        } else {
            displayMessage(getString(R.string.error_) + message)
        }
    }

    private fun validateExtraForErrors(): String {
        binding.apply {
            if (etExtraName.text.isNullOrBlank()) {
                return getString(R.string.the_extra_must_have_a_name)
            }
            if (existingWorkDateExtraList.isNotEmpty()) {
                for (extra in existingWorkDateExtraList) {
                    if (extra.wdeName == etExtraName.text.toString().trim()) {
                        return getString(R.string.this_extra_name_has_already_been_used)
                    }
                }
            }
            if (cf.getDoubleFromDollarOrPercentString(etValue.text.toString()) == 0.0) {
                return getString(R.string.this_extra_must_have_a_value)
            }
            return ANSWER_OK
        }
    }

    private fun displayMessage(message: String) {
        Toast.makeText(mView.context, message, Toast.LENGTH_LONG).show()
    }

    private fun getCurrentWorkDateExtra(): WorkDateExtras {
        binding.apply {
            return WorkDateExtras(
                cf.generateRandomIdAsLong(),
                curDateObject.workDateId,
                null,
                etExtraName.text.toString(),
                spAppliesTo.selectedItemPosition,
                1,
                cf.getDoubleFromDollarOrPercentString(etValue.text.toString()),
                chkIsCredit.isChecked,
                chkIsFixed.isChecked,
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun saveWorkDateExtra() {
        payDayViewModel.insertWorkDateExtra(
            getCurrentWorkDateExtra()
        )
        gotoCallingFragment()
    }

    private fun gotoCallingFragment() {
        gotoWorkDateUpdateFragment()
    }

    private fun gotoWorkDateUpdateFragment() {
        mView.findNavController().navigate(
            WorkDateExtraAddFragmentDirections.actionWorkDateExtraAddFragmentToWorkDateUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}