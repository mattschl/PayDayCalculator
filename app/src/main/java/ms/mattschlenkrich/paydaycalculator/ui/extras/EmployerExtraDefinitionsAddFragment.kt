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
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.CommonFunctions
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentEmployerExtraDefinitionsAddBinding
import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.model.WorkExtrasDefinitions


class EmployerExtraDefinitionsAddFragment : Fragment(
    R.layout.fragment_employer_extra_definitions_add
) {

    private var _binding: FragmentEmployerExtraDefinitionsAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var curEmployer: Employers
    private val df = DateFunctions()
    private val cf = CommonFunctions()
    private val extraList = ArrayList<WorkExtrasDefinitions>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployerExtraDefinitionsAddBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = "Add a definition"
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fillSpinners()
        fillMenu()
        fillValues()
        chooseDate()
        chooseFixedOrPercent()
    }

    private fun fillMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.save_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_save -> {
                        saveExtra()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun saveExtra() {
        binding.apply {
            val message = checkExtra()
            if (message == ANSWER_OK) {
                val curExtra = WorkExtrasDefinitions(
                    cf.generateId(),
                    curEmployer.employerId,
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
                mainActivity.workExtraViewModel.insertWorkExtraDefinition(
                    curExtra
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
        gotoExtraDefinitions()
    }

    private fun gotoExtraDefinitions() {
        mView.findNavController().navigate(
            EmployerExtraDefinitionsAddFragmentDirections
                .actionGlobalEmployerExtraDefinitionsFragment()
        )
    }

    private fun checkExtra(): String {
        binding.apply {
            var nameFound = false
            if (extraList.isNotEmpty()) {
                for (extra in extraList) {
                    if (extra.weName == etName.text.toString()) {
                        nameFound = true
                        break
                    }
                }
            }
            val errorMessage = if (etName.text.isNullOrBlank()) {
                "    ERROR!!\n" +
                        "The description cannot be blank"
            } else if (nameFound) {
                "    ERROR!! \n" +
                        "This already exists, change the description"
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
        curEmployer = mainActivity.mainViewModel.getEmployer()!!
        binding.apply {
            tvEmployer.text = curEmployer.employerName
            tvEffectiveDate.text = df.getCurrentDateAsString()
        }
        mainActivity.workExtraViewModel.getActiveWorkExtraDefinitions().observe(
            viewLifecycleOwner
        ) { workExtrasDefinitions ->
            extraList.clear()
            workExtrasDefinitions.listIterator().forEach {
                extraList.add(it)
            }

        }
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