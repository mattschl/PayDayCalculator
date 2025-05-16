package ms.mattschlenkrich.paycalculator.ui.extras.payperiodextra

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.model.extras.ExtraDefinitionAndType
import ms.mattschlenkrich.paycalculator.database.model.payperiod.PayPeriods
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDateExtraAndTypeAndDef
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkPayPeriodExtras
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentPayPeriodExtraAddBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity

class PayPeriodExtraAddFragment :
    Fragment(R.layout.fragment_pay_period_extra_add) {

    private var _binding: FragmentPayPeriodExtraAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var payDayViewModel: PayDayViewModel
    private lateinit var workExtraViewModel: WorkExtraViewModel
    private lateinit var curPayPeriod: PayPeriods
    private lateinit var curEmployer: Employers
    private lateinit var existingPayPeriodExtraList: List<WorkPayPeriodExtras>
    private lateinit var existingWorkDateExtraList: List<WorkDateExtraAndTypeAndDef>
    private lateinit var defaultExtraList: List<ExtraDefinitionAndType>
    private val df = DateFunctions()
    private val nf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPayPeriodExtraAddBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        payDayViewModel = mainActivity.payDayViewModel
        workExtraViewModel = mainActivity.workExtraViewModel
        mainActivity.title = getString(R.string.add_an_extra_to_this_pay_period)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        populateSpinners()
        if (mainViewModel.getEmployer() != null) {
            curEmployer = mainViewModel.getEmployer()!!
        }
        if (mainViewModel.getPayPeriod() != null) {
            curPayPeriod = mainViewModel.getPayPeriod()!!
        }
        populateExistingPayPeriodExtraList()
        populateExistingWorkDateExtraList()
        populateDefaultExtraList()
        val display = getString(R.string.cutoff_date_) +
                curPayPeriod.ppCutoffDate +
                getString(R.string._for_) +
                curEmployer.employerName
        binding.apply {
            lblPayInfo.text = display
            chkIsCredit.isChecked = mainActivity.mainViewModel.getIsCredit()
        }
    }

    private fun populateSpinners() {
        binding.apply {
            val frequencyAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_bold,
                resources.getStringArray(R.array.attach_to_frequencies)
            )
            frequencyAdapter.setDropDownViewResource(R.layout.spinner_item_bold)
            spAppliesTo.adapter = frequencyAdapter
        }
    }

    private fun populateExistingPayPeriodExtraList() {
        payDayViewModel.getPayPeriodExtras(curPayPeriod.payPeriodId)
            .observe(viewLifecycleOwner) { list ->
                existingPayPeriodExtraList = list
            }
    }

    private fun populateExistingWorkDateExtraList() {
        payDayViewModel.getWorkDateExtrasPerPay(
            curEmployer.employerId, curPayPeriod.ppCutoffDate
        ).observe(viewLifecycleOwner) { list ->
            existingWorkDateExtraList = list
        }
    }

    private fun populateDefaultExtraList() {
        workExtraViewModel.getDefaultExtraTypesAndCurrentDef(
            curEmployer.employerId, curPayPeriod.ppCutoffDate
        ).observe(viewLifecycleOwner) { list ->
            defaultExtraList = list
        }
    }

    private fun setClickActions() {
        setMenuActions()
        binding.chkIsFixed.setOnClickListener { setFixedOrPercent() }
    }

    private fun setMenuActions() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.save_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_save -> {
                        prepareToSaveExtraIfValidationsSucceed()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun setFixedOrPercent() {
        binding.apply {
            etValue.setText(
                if (chkIsFixed.isChecked) {
                    nf.displayDollars(
                        nf.getDoubleFromDollarOrPercentString(
                            etValue.text.toString()
                        )
                    )
                } else {
                    nf.getPercentStringFromDouble(
                        nf.getDoubleFromDollarOrPercentString(
                            etValue.text.toString()
                        ) / 100
                    )
                }
            )
        }
    }

    private fun prepareToSaveExtraIfValidationsSucceed() {
        val message = validateExtraForErrors()
        if (message == ANSWER_OK) {
            validateFromExistingExtrasAndContinue()
        } else {
            displayMessage(getString(R.string.error_) + message)
        }
    }

    private fun validateExtraForErrors(): String {
        binding.apply {
            if (etExtraName.text.isNullOrBlank()
            ) {
                return getString(R.string.the_extra_must_have_a_name)
            }
            if (existingPayPeriodExtraList.isNotEmpty()) {
                for (extra in existingPayPeriodExtraList) {
                    if (extra.ppeName == etExtraName.text.toString().trim()) {
                        return getString(R.string.this_extra_name_has_already_been_used)
                    }
                }
            }
            if (etValue.text.isNullOrBlank() ||
                nf.getDoubleFromDollarOrPercentString(etValue.text.toString()) == 0.0
            ) {
                return getString(R.string.this_extra_must_have_a_value)
            }
            return ANSWER_OK
        }
    }

    private fun displayMessage(message: String) {
        Toast.makeText(mView.context, message, Toast.LENGTH_LONG).show()
    }

    private fun validateFromExistingExtrasAndContinue() {
        if (existingWorkDateExtraList.isNotEmpty()) {
            binding.apply {
                var continueOn = true
                for (extra in existingWorkDateExtraList) {
                    if (etExtraName.text.toString().trim() == extra.extra.wdeName
                    ) {
                        continueOn = false
                        chooseToAddInAdditionToExistingExtra(
                            extra.extra.wdeName,
                            getString(R.string.this_is_already_used_for_this_payday__add)
                        )
                        break
                    }
                }
                if (continueOn) {
                    validateFromExistingDefaultExtrasAndContinueToSave()
                }
            }
        } else {
            validateFromExistingDefaultExtrasAndContinueToSave()
        }
    }

    private fun validateFromExistingDefaultExtrasAndContinueToSave() {
        if (defaultExtraList.isNotEmpty()) {
            binding.apply {
                var continueOn = true
                for (extra in defaultExtraList) {
                    if (etExtraName.text.toString().trim() == extra.extraType.wetName) {
                        continueOn = false
                        chooseToAddInAdditionToExistingExtra(
                            extra.extraType.wetName,
                            getString(R.string.this_is_already_used_for_this_payday__overwrite)
                        )
                        break
                    }
                }
                if (continueOn) {
                    saveExtraAndGotoCallingFragment()
                }
            }
        } else {
            saveExtraAndGotoCallingFragment()
        }
    }

    private fun chooseToAddInAdditionToExistingExtra(extraName: String, message: String) {
        AlertDialog.Builder(mView.context)
            .setTitle(
                getString(R.string.confirm_adding_duplicate_extra__) +
                        extraName
            )
            .setMessage(message)
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                saveExtraAndGotoCallingFragment()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun saveExtraAndGotoCallingFragment() {
        binding.apply {
            payDayViewModel.insertPayPeriodExtra(
                getCurrentPayPeriodExtra()
            )
        }
        gotoCallingFragment()
    }

    private fun getCurrentPayPeriodExtra(): WorkPayPeriodExtras {
        binding.apply {
            return WorkPayPeriodExtras(
                nf.generateRandomIdAsLong(),
                curPayPeriod.payPeriodId,
                null,
                etExtraName.text.toString().trim(),
                spAppliesTo.selectedItemPosition,
                3,
                nf.getDoubleFromDollarOrPercentString(
                    etValue.text.toString()
                ),
                chkIsFixed.isChecked,
                chkIsCredit.isChecked,
                false,
                df.getCurrentTimeAsString(),
            )
        }
    }

    private fun gotoCallingFragment() {
        gotoPayDetailsFragment()
    }

    private fun gotoPayDetailsFragment() {
        mView.findNavController().navigate(
            PayPeriodExtraAddFragmentDirections
                .actionPayPeriodExtraAddFragmentToPayDetailFragmentNew()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}