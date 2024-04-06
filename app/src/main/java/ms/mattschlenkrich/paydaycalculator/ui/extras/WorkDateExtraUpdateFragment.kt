package ms.mattschlenkrich.paydaycalculator.ui.extras

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
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkDateExtraUpdateBinding
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDateExtras
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class WorkDateExtraUpdateFragment : Fragment(R.layout.fragment_work_date_extra_update) {

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
        mainActivity.title = "Update this Extra"
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fillSpinners()
        setActions()
        chooseFixedOrPercent()
        fillMenu()
        fillValues()
    }

    private fun setActions() {
        binding.apply {
            fabDone.setOnClickListener {
                updateWorkDateExtra()
            }
        }
    }

    private fun updateWorkDateExtra() {
        val message = checkExtra()
        if (message == ANSWER_OK) {
            mainActivity.payDayViewModel.updateWorkDateExtra(
                getCurWorkDateExtra()
            )
            gotoWorkDateUpdate()
        } else {
            Toast.makeText(
                mView.context,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun checkExtra(): String {
        binding.apply {
            var nameFound = false
            if (extraList.isNotEmpty()) {
                for (extra in extraList) {
                    if (extra.wdeName == etExtraName.text.toString().trim() &&
                        etExtraName.text.toString().trim() != oldWorkDateExtra.wdeName
                    ) {
                        nameFound = true
                        break
                    }
                }
            }
            val errorMessage = if (etExtraName.text.isNullOrBlank()) {
                "    ERROR!!\n" +
                        "The Extra must have a name"
            } else if (nameFound) {
                "   ERROR!!\n" +
                        "This Extra name has already been used. \n" +
                        "Choose a different name."
            } else if (cf.getDoubleFromDollarOrPercent(etValue.text.toString()) == 0.0) {
                "   ERROR!!\n" +
                        "This Extra must have a value"
            } else {
                ANSWER_OK
            }
            return errorMessage
        }
    }

    private fun chooseFixedOrPercent() {
        binding.apply {
            chkIsFixed.setOnClickListener {
                etValue.setText(
                    if (chkIsFixed.isChecked) {
                        cf.displayDollars(
                            cf.getDoubleFromDollarOrPercent(
                                etValue.text.toString()
                            )
                        )
                    } else {
                        cf.displayPercentFromDouble(
                            cf.getDoubleFromDollarOrPercent(
                                etValue.text.toString()
                            ) / 100
                        )
                    }
                )
            }
        }
    }

    private fun fillValues() {
        extraList = mainActivity.mainViewModel.getWorkDateExtraList()
        if (mainActivity.mainViewModel.getWorkDateObject() != null &&
            mainActivity.mainViewModel.getWorkDateExtra() != null &&
            mainActivity.mainViewModel.getWorkDateString() != null
        ) {
            curDateObject =
                mainActivity.mainViewModel.getWorkDateObject()!!
            oldWorkDateExtra =
                mainActivity.mainViewModel.getWorkDateExtra()!!
            mainActivity.title = "Update Extra: ${oldWorkDateExtra.wdeName}"
            binding.apply {
                var display = "Date: ${
                    df.getDisplayDate(
                        mainActivity.mainViewModel.getWorkDateString()!!
                    )
                } " +
                        "Employer: ${mainActivity.mainViewModel.getEmployerString()}"
                lblDateInfo.text = display
                etExtraName.setText(oldWorkDateExtra.wdeName)
                spAppliesTo.setSelection(oldWorkDateExtra.wdeAppliesTo)
                display = if (oldWorkDateExtra.wdeIsFixed) {
                    cf.displayDollars(oldWorkDateExtra.wdeValue)
                } else {
                    cf.displayPercentFromDouble(oldWorkDateExtra.wdeValue)
                }
                etValue.setText(display)
                chkIsFixed.isChecked = oldWorkDateExtra.wdeIsFixed
                chkIsCredit.isChecked = oldWorkDateExtra.wdeIsCredit
            }
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
        binding.apply {
            val extra = getCurWorkDateExtra()
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

    private fun gotoWorkDateUpdate() {
        mainActivity.mainViewModel.setWorkDateExtra(null)
        mView.findNavController().navigate(
            WorkDateExtraUpdateFragmentDirections
                .actionWorkDateExtraUpdateFragmentToWorkDateUpdateFragment()
        )
    }

    private fun getCurWorkDateExtra(): WorkDateExtras {
        binding.apply {
            val value = if (
                cf.getDoubleFromDollarOrPercent(etValue.text.toString()) >= 1.0 &&
                !chkIsFixed.isChecked
            ) {
                cf.getDoubleFromDollarOrPercent(etValue.text.toString()) / 100
            } else {
                cf.getDoubleFromDollarOrPercent(etValue.text.toString())
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

    private fun fillSpinners() {
        binding.apply {
            val frequencies = ArrayList<String>()
            for (i in 0..1) {
                frequencies.add(
                    resources.getStringArray(R.array.pay_per_frequencies)[i]
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}