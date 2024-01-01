package ms.mattschlenkrich.paydaycalculator.ui.extras

import android.app.DatePickerDialog
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
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.CommonFunctions
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentEmployerExtraDefinitionUpdateBinding
import ms.mattschlenkrich.paydaycalculator.model.ExtraDefinitionFull
import ms.mattschlenkrich.paydaycalculator.model.WorkExtrasDefinitions

class EmployerExtraDefinitionUpdateFragment :
    Fragment(R.layout.fragment_employer_extra_definition_update) {

    private var _binding: FragmentEmployerExtraDefinitionUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var curExtraDefinitionFull: ExtraDefinitionFull
    private val df = DateFunctions()
    private val cf = CommonFunctions()
    private val definitionList = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployerExtraDefinitionUpdateBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fillSpinners()
        fillMenu()
        fillValues()
        chooseDate()
        chooseFixedOrPercent()
        setActions()
    }

    private fun setActions() {
        binding.apply {
            fabDone.setOnClickListener {
                updateDefinition()
            }
        }
    }

    private fun updateDefinition() {
        val message = checkExtra()
        if (message == ANSWER_OK) {
            mainActivity.workExtraViewModel.updateWorkExtraDefinition(
                getCurrentDefinition()
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

    private fun getCurrentDefinition(): WorkExtrasDefinitions {
        binding.apply {
            return WorkExtrasDefinitions(
                curExtraDefinitionFull.definition.workExtraId,
                curExtraDefinitionFull.definition.weEmployerId,
                etName.text.toString(),
                spAppliesTo.selectedItemPosition,
                spAttachTo.selectedItemPosition,
                cf.getDoubleFromDollarOrPercent(etValue.text.toString()),
                chkIsFixed.isChecked,
                chkIsCredit.isChecked,
                chkIsDefault.isChecked,
                tvEffectiveDate.text.toString(),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun checkExtra(): String {
        binding.apply {
            var nameFound = false
            if (definitionList.isNotEmpty()) {
                for (name in definitionList) {
                    if (name == etName.text.toString().trim()) {
                        nameFound = true
                        break
                    }
                }
            }
            val errorMessage = if (etName.text.isNullOrBlank()) {
                "    ERROR!!\n" +
                        "There needs to be a description!"
            } else if (nameFound && etName.text.toString() !=
                curExtraDefinitionFull.definition.weName
            ) {
                "    ERROE!!\n" +
                        "This extra item already exists!"
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

    private fun chooseDate() {
        binding.apply {
            tvEffectiveDate.setOnClickListener {
                val curDateAll = tvEffectiveDate.text.toString()
                    .split("-")
                val datePickerDialog = DatePickerDialog(
                    mView.context,
                    { _, year, monthOfYear, dayOfMonth ->
                        val month = monthOfYear + 1
                        val display = "$year-${
                            month.toString()
                                .padStart(2, '0')
                        }-${
                            dayOfMonth.toString().padStart(2, '0')
                        }"
                        tvEffectiveDate.text = display
                    },
                    curDateAll[0].toInt(),
                    curDateAll[1].toInt() - 1,
                    curDateAll[2].toInt()
                )
                datePickerDialog.setTitle("Choose when this will take effect")
                datePickerDialog.show()
            }
        }
    }

    private fun fillValues() {
        binding.apply {
            if (mainActivity.mainViewModel.getExtraDefinitionFull() != null) {
                curExtraDefinitionFull =
                    mainActivity.mainViewModel.getExtraDefinitionFull()!!
                mainActivity.workExtraViewModel.getWorkExtraDefinitions(
                    curExtraDefinitionFull.employer.employerId
                ).observe(
                    viewLifecycleOwner
                ) { definition ->
                    definitionList.clear()
                    definition.listIterator().forEach {
                        definitionList.add(it.weName)
                    }
                }
                tvEmployer.text = curExtraDefinitionFull.employer.employerName
                etName.setText(curExtraDefinitionFull.definition.weName)
                spAppliesTo.setSelection(curExtraDefinitionFull.definition.weAppliesTo)
                spAttachTo.setSelection(curExtraDefinitionFull.definition.weAttachTo)
                etValue.setText(
                    if (curExtraDefinitionFull.definition.weIsFixed) {
                        cf.displayDollars(curExtraDefinitionFull.definition.weValue)
                    } else {
                        cf.displayPercentFromDouble(curExtraDefinitionFull.definition.weValue)
                    }
                )
                chkIsFixed.isChecked = curExtraDefinitionFull.definition.weIsFixed
                chkIsCredit.isChecked = curExtraDefinitionFull.definition.weIsCredit
                chkIsDefault.isChecked = curExtraDefinitionFull.definition.weIsDefault
                tvEffectiveDate.text = curExtraDefinitionFull.definition.weEffectiveDate
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
        mainActivity.workExtraViewModel.deleteWorkExtraDefinition(
            curExtraDefinitionFull.definition.workExtraId,
            df.getCurrentTimeAsString()
        )
        gotoCallingFragment()
    }

    private fun gotoCallingFragment() {
        mView.findNavController().navigate(
            EmployerExtraDefinitionUpdateFragmentDirections
                .actionEmployerExtraDefinitionUpdateFragmentToEmployerExtraDefinitionsFragment()
        )
    }

    private fun fillSpinners() {
        binding.apply {
            val frequencyAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_bold,
                resources.getStringArray(R.array.extra_frequencies)
            )
            frequencyAdapter.setDropDownViewResource(R.layout.spinner_item_bold)
            spAppliesTo.adapter = frequencyAdapter
            spAttachTo.adapter = frequencyAdapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}