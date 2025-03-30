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
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkDateExtraUpdateBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity

class WorkDateExtraUpdateFragment
    : Fragment(R.layout.fragment_work_date_extra_update) {

    private var _binding: FragmentWorkDateExtraUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var curDateObject: WorkDates
    private lateinit var oldWorkDateExtra: WorkDateExtras
    private var extraList = ArrayList<WorkDateExtras>()
    private val df = DateFunctions()
    private val cf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkDateExtraUpdateBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.update_this_extra)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        populateSpinners()
        extraList = mainActivity.mainViewModel.getWorkDateExtraList()
        populateInfoDisplay()
    }

    private fun populateInfoDisplay() {
        if (mainActivity.mainViewModel.getWorkDateObject() != null &&
            mainActivity.mainViewModel.getWorkDateExtra() != null &&
            mainActivity.mainViewModel.getWorkDateString() != null
        ) {
            curDateObject =
                mainActivity.mainViewModel.getWorkDateObject()!!
            oldWorkDateExtra =
                mainActivity.mainViewModel.getWorkDateExtra()!!
            mainActivity.title = getString(R.string.update_extra_) +
                    oldWorkDateExtra.wdeName
            binding.apply {
                var display = getString(R.string.date_) +
                        df.getDisplayDate(
                            mainActivity.mainViewModel.getWorkDateString()!!
                        ) +
                        getString(R.string.employer_) +
                        mainActivity.mainViewModel.getEmployerString()
                lblDateInfo.text = display
                etExtraName.setText(oldWorkDateExtra.wdeName)
                spAppliesTo.setSelection(oldWorkDateExtra.wdeAppliesTo)
                display = if (oldWorkDateExtra.wdeIsFixed) {
                    cf.displayDollars(oldWorkDateExtra.wdeValue)
                } else {
                    cf.getPercentStringFromDouble(oldWorkDateExtra.wdeValue)
                }
                etValue.setText(display)
                chkIsFixed.isChecked = oldWorkDateExtra.wdeIsFixed
                chkIsCredit.isChecked = oldWorkDateExtra.wdeIsCredit
            }
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
                mView.context, R.layout.spinner_item_bold,
                frequencies
            )
            frequencyAdapter.setDropDownViewResource(R.layout.spinner_item_bold)
            spAppliesTo.adapter = frequencyAdapter
        }
    }

    private fun setClickActions() {
        setMenuActions()
        binding.apply {
            fabDone.setOnClickListener {
                updateWorkDateExtraIfValid()
            }
        }
        chooseFixedOrPercent()
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

    private fun chooseFixedOrPercent() {
        binding.chkIsFixed.setOnClickListener {
            changeTextToFixedOrPercentString()
        }
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


    private fun deleteExtra() {
        binding.apply {
            val extra = getCurrentWorkDateExtra()
            mainActivity.payDayViewModel.updateWorkDateExtra(
                WorkDateExtras(
                    extra.workDateExtraId,
                    extra.wdeWorkDateId,
                    extra.wdeExtraTypeId,
                    extra.wdeName,
                    extra.wdeAppliesTo,
                    extra.wdeAttachTo,
                    extra.wdeValue,
                    extra.wdeIsFixed,
                    extra.wdeIsCredit,
                    true,
                    df.getCurrentTimeAsString()
                )
            )
        }
        gotoWorkDateUpdate()
    }

    private fun updateWorkDateExtraIfValid() {
        val message = validateExtraForErrors()
        if (message == ANSWER_OK) {
            updateWorkDateExtra()
            gotoWorkDateUpdate()
        } else {
            displayMessage(getString(R.string.error_) + message)
        }
    }

    private fun validateExtraForErrors(): String {
        binding.apply {
            if (etExtraName.text.isNullOrBlank()) {
                return getString(R.string.the_extra_must_have_a_name)
            }
            if (extraList.isNotEmpty()) {
                for (extra in extraList) {
                    if (extra.wdeName == etExtraName.text.toString().trim() &&
                        etExtraName.text.toString().trim() != oldWorkDateExtra.wdeName
                    ) {
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
        Toast.makeText(
            mView.context,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun getCurrentWorkDateExtra(): WorkDateExtras {
        binding.apply {
            val value = if (
                cf.getDoubleFromDollarOrPercentString(etValue.text.toString()) >= 1.0 &&
                !chkIsFixed.isChecked
            ) {
                cf.getDoubleFromDollarOrPercentString(etValue.text.toString()) / 100
            } else {
                cf.getDoubleFromDollarOrPercentString(etValue.text.toString())
            }
            return WorkDateExtras(
                oldWorkDateExtra.workDateExtraId,
                oldWorkDateExtra.wdeWorkDateId,
                oldWorkDateExtra.wdeExtraTypeId,
                etExtraName.text.toString().trim(),
                spAppliesTo.selectedItemPosition,
                1,
                value,
                chkIsFixed.isChecked,
                chkIsCredit.isChecked,
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun updateWorkDateExtra() {
        mainActivity.payDayViewModel.updateWorkDateExtra(
            getCurrentWorkDateExtra()
        )
    }

    private fun gotoWorkDateUpdate() {
        mainActivity.mainViewModel.setWorkDateExtra(null)
        gotoWorkDateUpdateFragment()
    }

    private fun gotoWorkDateUpdateFragment() {
        mView.findNavController().navigate(
            WorkDateExtraUpdateFragmentDirections
                .actionWorkDateExtraUpdateFragmentToWorkDateUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}