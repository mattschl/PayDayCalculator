package ms.mattschlenkrich.paycalculator.ui.extras

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
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.model.payperiod.PayPeriods
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkPayPeriodExtras
import ms.mattschlenkrich.paycalculator.databinding.FragmentPayPeriodExtraUpdateBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity


class PayPeriodExtraUpdateFragment : Fragment(R.layout.fragment_pay_period_extra_update) {

    private var _binding: FragmentPayPeriodExtraUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var curPayPeriod: PayPeriods
    private lateinit var curEmployer: Employers
    private lateinit var oldPayPeriodExtra: WorkPayPeriodExtras
    private lateinit var existingExtraList: List<WorkPayPeriodExtras>
    private val df = DateFunctions()
    private val nf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPayPeriodExtraUpdateBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.update_extra_for_this_pay_period)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        populateSpinners()
        if (mainActivity.mainViewModel.getEmployer() != null) {
            curEmployer = mainActivity.mainViewModel.getEmployer()!!
        }
        if (mainActivity.mainViewModel.getPayPeriod() != null) {
            curPayPeriod = mainActivity.mainViewModel.getPayPeriod()!!
        }
        populateExistingExtraList()
        if (mainActivity.mainViewModel.getPayPeriodExtra() != null) {
            oldPayPeriodExtra = mainActivity.mainViewModel.getPayPeriodExtra()!!
            binding.apply {
                mainActivity.title = getString(R.string.update_extra_) +
                        oldPayPeriodExtra.ppeName
                var display = getString(R.string.pay_cutoff_) +
                        curPayPeriod.ppCutoffDate +
                        getString(R.string.employer_) +
                        curEmployer.employerName
                lblPayInfo.text = display
                etExtraName.setText(oldPayPeriodExtra.ppeName)
                spAppliesTo.setSelection(oldPayPeriodExtra.ppeAppliesTo)
                display = if (oldPayPeriodExtra.ppeIsFixed) {
                    nf.displayDollars(oldPayPeriodExtra.ppeValue)
                } else {
                    nf.getPercentStringFromDouble(oldPayPeriodExtra.ppeValue)
                }
                etValue.setText(display)
                chkIsFixed.isChecked = oldPayPeriodExtra.ppeIsFixed
                chkIsCredit.isChecked = oldPayPeriodExtra.ppeIsCredit
            }
        }
    }

    private fun populateExistingExtraList() {
        mainActivity.payDayViewModel.getPayPeriodExtras(curPayPeriod.payPeriodId)
            .observe(viewLifecycleOwner) { list ->
                existingExtraList = list
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

    private fun setClickActions() {
        setMenuActions()
        binding.apply {
            chkIsFixed.setOnClickListener {
                setFixedOrPercent()
            }
            fabDone.setOnClickListener {
                updatePayPeriodExtraIfValid()
            }
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
                        deleteExtra()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun deleteExtra() {
        mainActivity.payDayViewModel.deletePayPeriodExtra(
            oldPayPeriodExtra.workPayPeriodExtraId, df.getCurrentTimeAsString()
        )
        gotoCallingFragment()
    }

    private fun updatePayPeriodExtraIfValid() {
        val message = validateExtra()
        if (message == ANSWER_OK) {
            updatePayPeriodExtra()
            gotoPayDetail()
        } else {
            displayError(message)
        }
    }

    private fun displayError(message: String) {
        Toast.makeText(
            mView.context,
            getString(R.string.error_) + message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun validateExtra(): String {
        binding.apply {
            if (etExtraName.text.isNullOrBlank()) {
                return getString(R.string.the_extra_must_have_a_name)
            }
            if (existingExtraList.isNotEmpty()) {
                for (extra in existingExtraList) {
                    if (extra.ppeName == etExtraName.text.toString().trim() &&
                        etExtraName.text.toString().trim() != oldPayPeriodExtra.ppeName
                    ) {
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

    private fun updatePayPeriodExtra() {
        mainActivity.payDayViewModel.updatePayPeriodExtra(
            getCurrentPayPeriodExtra()
        )
    }

    private fun getCurrentPayPeriodExtra(): WorkPayPeriodExtras {
        binding.apply {
            return WorkPayPeriodExtras(
                oldPayPeriodExtra.workPayPeriodExtraId,
                oldPayPeriodExtra.ppePayPeriodId,
                oldPayPeriodExtra.ppeExtraTypeId,
                etExtraName.text.toString().trim(),
                spAppliesTo.selectedItemPosition,
                3,
                nf.getDoubleFromDollarOrPercentString(
                    etValue.text.toString()
                ),
                chkIsFixed.isChecked,
                chkIsCredit.isChecked,
                false,
                df.getCurrentTimeAsString()
            )
        }
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

    private fun gotoCallingFragment() {
        gotoPayDetail()
    }

    private fun gotoPayDetail() {
        mainActivity.mainViewModel.clearPayPeriodExtraList()
        mainActivity.mainViewModel.setPayPeriodExtra(null)
        gotoPayDetailFragment()
    }

    private fun gotoPayDetailFragment() {
        mView.findNavController().navigate(
            PayPeriodExtraUpdateFragmentDirections
                .actionPayPeriodExtraUpdateFragmentToPayDetailFragmentNew()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}