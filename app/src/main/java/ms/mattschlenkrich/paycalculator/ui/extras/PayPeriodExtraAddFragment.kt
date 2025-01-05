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
import ms.mattschlenkrich.paycalculator.database.model.payperiod.PayPeriods
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkPayPeriodExtras
import ms.mattschlenkrich.paycalculator.databinding.FragmentPayPeriodExtraAddBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity

class PayPeriodExtraAddFragment :
    Fragment(R.layout.fragment_pay_period_extra_add) {

    private var _binding: FragmentPayPeriodExtraAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var curPayPeriod: PayPeriods
    private lateinit var curEmployer: Employers
    private var extraList = ArrayList<WorkPayPeriodExtras>()
    private val df = DateFunctions()
    private val cf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPayPeriodExtraAddBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
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
        if (mainActivity.mainViewModel.getEmployer() != null) {
            curEmployer = mainActivity.mainViewModel.getEmployer()!!
        }
        if (mainActivity.mainViewModel.getPayPeriod() != null) {
            curPayPeriod = mainActivity.mainViewModel.getPayPeriod()!!
        }
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
                resources.getStringArray(R.array.pay_per_frequencies)
            )
            frequencyAdapter.setDropDownViewResource(R.layout.spinner_item_bold)
            spAppliesTo.adapter = frequencyAdapter
        }
    }

    private fun setClickActions() {
        setMenuActions()
        chooseFixedOrPercent()
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
                        saveExtraIfValid()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun chooseFixedOrPercent() {
        binding.apply {
            chkIsFixed.setOnClickListener {
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
    }

    private fun saveExtraIfValid() {
        val message = validateExtra()
        if (message == ANSWER_OK) {
            saveExtra()
            gotoPayDetailsFragment()
        } else {
            Toast.makeText(
                mView.context,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun validateExtra(): String {
        binding.apply {
            if (etExtraName.text.isNullOrBlank()) {
                return getString(R.string.error_) +
                        getString(R.string.the_extra_must_have_a_name)
            }
            if (extraList.isNotEmpty()) {
                for (extra in extraList) {
                    if (extra.ppeName == etExtraName.text.toString().trim()) {
                        return getString(R.string.error_) +
                                getString(R.string.this_extra_name_has_already_been_used)
                    }
                }
            }
            if (cf.getDoubleFromDollarOrPercentString(etValue.text.toString()) == 0.0) {
                getString(R.string.error_) +
                        getString(R.string.this_extra_must_have_a_value)
            }
            return ANSWER_OK
        }
    }

    private fun getCurrentPayPeriodExtra(): WorkPayPeriodExtras {
        binding.apply {
            return WorkPayPeriodExtras(
                cf.generateRandomIdAsLong(),
                curPayPeriod.payPeriodId,
                null,
                etExtraName.text.toString().trim(),
                spAppliesTo.selectedItemPosition,
                3,
                cf.getDoubleFromDollarOrPercentString(
                    etValue.text.toString()
                ),
                chkIsFixed.isChecked,
                chkIsCredit.isChecked,
                false,
                df.getCurrentTimeAsString(),
            )
        }
    }

    private fun saveExtra() {
        binding.apply {
            mainActivity.payDayViewModel.insertPayPeriodExtra(
                getCurrentPayPeriodExtra()
            )
        }
    }

    private fun gotoPayDetailsFragment() {
        mView.findNavController().navigate(
            PayPeriodExtraAddFragmentDirections
                .actionPayPeriodExtraAddFragmentToPayDetailFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}