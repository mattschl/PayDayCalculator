package ms.mattschlenkrich.paycalculator.ui.extras.employerextras

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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
import ms.mattschlenkrich.paycalculator.database.model.extras.ExtraDefTypeAndEmployer
import ms.mattschlenkrich.paycalculator.database.model.extras.WorkExtrasDefinitions
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentEmployerExtraDefinitionUpdateBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity

class EmployerExtraDefinitionUpdateFragment :
    Fragment(R.layout.fragment_employer_extra_definition_update) {

    private var _binding: FragmentEmployerExtraDefinitionUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workExtraViewModel: WorkExtraViewModel
    private lateinit var curEmployer: Employers
    private lateinit var curExtraDefinitionFull: ExtraDefTypeAndEmployer
    private val df = DateFunctions()
    private val nf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployerExtraDefinitionUpdateBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workExtraViewModel = mainActivity.workExtraViewModel
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        curEmployer = mainViewModel.getEmployer()!!
        binding.apply {
            etName.isEnabled = false
            if (mainViewModel.getExtraDefinitionFull() != null) {
                curExtraDefinitionFull = mainViewModel.getExtraDefinitionFull()!!
                tvEmployer.text = curExtraDefinitionFull.employer.employerName
                etName.setText(curExtraDefinitionFull.extraType.wetName)
                etValue.setText(
                    if (curExtraDefinitionFull.definition.weIsFixed) {
                        nf.displayDollars(curExtraDefinitionFull.definition.weValue)
                    } else {
                        nf.getPercentStringFromDouble(curExtraDefinitionFull.definition.weValue)
                    }
                )
                chkIsFixed.isChecked = curExtraDefinitionFull.definition.weIsFixed
                populateFromExtraList()
                tvEffectiveDate.text = curExtraDefinitionFull.definition.weEffectiveDate
            }
        }
    }

    private fun populateFromExtraList() {
        binding.apply {
            var display = if (curExtraDefinitionFull.extraType.wetIsCredit) {
                getString(R.string.credit)
            } else {
                getString(R.string.debit)
            }
            display += " " + getString(R.string.calculated) + resources.getStringArray(R.array.applies_to_frequencies)[curExtraDefinitionFull.extraType.wetAppliesTo] + getString(
                R.string.period_space
            ) + getString(R.string._attaches_to_) + resources.getStringArray(R.array.attach_to_frequencies)[curExtraDefinitionFull.extraType.wetAttachTo] + getString(
                R.string.period_hyphen
            )
            display += if (curExtraDefinitionFull.extraType.wetIsDefault) {
                getString(R.string.is_automatic)
            } else {
                getString(R.string.added_manually)
            }
            tvDescription.text = display
            when (curExtraDefinitionFull.extraType.wetAppliesTo) {
                4 -> {
                    chkIsFixed.isChecked = false
                    chkIsFixed.text = getString(R.string.defaults_to_percentage)
                    chkIsFixed.isEnabled = false
                }

                1 -> {
                    chkIsFixed.isChecked = true
                    chkIsFixed.text = getString(R.string.defaults_to_fixed)
                    chkIsFixed.isEnabled = false
                }

                else -> {
                    chkIsFixed.text = getString(R.string.check_if_this_is_a_fixed_amount)
                    chkIsFixed.isEnabled = true
                }
            }
        }
    }

    private fun setClickActions() {
        setMenuActions()
        binding.apply {
            fabDone.setOnClickListener {
                updateDefinitionIfValid()
            }
            tvEffectiveDate.setOnClickListener {
                changeEffectiveDate()
            }
            chooseFixedOrPercent()
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

    private fun chooseFixedOrPercent() {
        binding.apply {
            chkIsFixed.setOnClickListener {
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
    }

    private fun changeEffectiveDate() {
        binding.apply {
            val curDateAll = tvEffectiveDate.text.toString().split("-")
            val datePickerDialog = DatePickerDialog(
                mView.context, { _, year, monthOfYear, dayOfMonth ->
                    val month = monthOfYear + 1
                    val display = "$year-${
                        month.toString().padStart(2, '0')
                    }-${
                        dayOfMonth.toString().padStart(2, '0')
                    }"
                    tvEffectiveDate.text = display
                }, curDateAll[0].toInt(), curDateAll[1].toInt() - 1, curDateAll[2].toInt()
            )
            datePickerDialog.setTitle(getString(R.string.choose_when_this_will_take_effect))
            datePickerDialog.show()
        }
    }

    private fun updateDefinitionIfValid() {
        val message = validateExtraDefinition()
        if (message == ANSWER_OK) {
            updateDefinition()
            gotoEmployerExtraDefinitionsFragment()
        } else {
            displayMessage(getString(R.string.error_) + message)
        }
    }

    private fun displayMessage(message: String) {
        Toast.makeText(mView.context, message, Toast.LENGTH_LONG).show()
    }

    private fun getCurrentDefinition(): WorkExtrasDefinitions {
        binding.apply {
            val curDef = curExtraDefinitionFull.definition
            return WorkExtrasDefinitions(
                curDef.workExtraDefId,
                curDef.weEmployerId,
                curDef.weExtraTypeId,
                nf.getDoubleFromDollarOrPercentString(etValue.text.toString()),
                chkIsFixed.isChecked,
                tvEffectiveDate.text.toString(),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun validateExtraDefinition(): String {
        binding.apply {
            if (etValue.text.isNullOrBlank() || nf.getDoubleFromDollarOrPercentString(etValue.text.toString()) == 0.0) {
                return getString(R.string.please_enter_a_value_for_this_extra)
            }
            return ANSWER_OK
        }
    }

    private fun updateDefinition() {
        workExtraViewModel.updateWorkExtraDefinition(
            getCurrentDefinition()
        )
    }

    private fun deleteExtra() {
        workExtraViewModel.deleteWorkExtraDefinition(
            curExtraDefinitionFull.definition.workExtraDefId, df.getCurrentTimeAsString()
        )
        gotoEmployerExtraDefinitionsFragment()
    }

    private fun gotoEmployerExtraDefinitionsFragment() {
        mView.findNavController().navigate(
            EmployerExtraDefinitionUpdateFragmentDirections.actionEmployerExtraDefinitionUpdateFragmentToEmployerExtraDefinitionsFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}