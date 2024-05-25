package ms.mattschlenkrich.paydaycalculator.ui.extras

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
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.FRAG_EXTRA_DEFINITIONS
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.common.WAIT_250
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentEmployerExtraDefinitionsAddBinding
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paydaycalculator.model.extras.WorkExtrasDefinitions
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity


class EmployerExtraDefinitionsAddFragment : Fragment(
    R.layout.fragment_employer_extra_definitions_add
) {

    private var _binding: FragmentEmployerExtraDefinitionsAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var curEmployer: Employers
    private val df = DateFunctions()
    private val cf = NumberFunctions()
    private val extraList = ArrayList<WorkExtraTypes>()

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
        fillMenu()
        fillSpinners()
        chooseDate()
        chooseFixedOrPercent()
        chooseExtraType()
        fillValues()
        changeDate()
    }

    private fun chooseExtraType() {
        binding.apply {
            spExtraTypes.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (spExtraTypes.adapter.count > 0 &&
                            spExtraTypes.selectedItem.toString() ==
                            getString(R.string.add_a_new_extra_type)
                        ) {
                            gotoExtraTypeAdd()
                        } else {
                            fillFromExtraList()
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        //not needed
                    }
                }
        }
    }

    private fun fillFromExtraList() {
        binding.apply {
            for (extra in extraList) {
                if (extra.wetName == spExtraTypes.selectedItem.toString()) {
                    var display = if (extra.wetIsCredit) {
                        "Credit"
                    } else {
                        "Debit"
                    }
                    display += " - Calculated " +
                            "${
                                resources.getStringArray(
                                    R.array.pay_per_frequencies
                                )[extra.wetAppliesTo]
                            }. " +
                            "Attaches to ${
                                resources.getStringArray(
                                    R.array.pay_per_frequencies
                                )[extra.wetAttachTo]
                            }. - "
                    display += if (extra.wetIsDefault) "Is automatic"
                    else "Added Manually"
                    tvDescription.text = display
                }
            }
        }
    }

    private fun gotoExtraTypeAdd() {
        mainActivity.mainViewModel.setEmployer(curEmployer)
        mView.findNavController().navigate(
            EmployerExtraDefinitionsAddFragmentDirections
                .actionEmployerExtraDefinitionsAddFragmentToWorkExtraTypeAddFragment()
        )
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

    private fun getCurExtraDef(): WorkExtrasDefinitions {
        binding.apply {
            var extraId = 0L
            for (extra in extraList) {
                if (extra.wetName == spExtraTypes.selectedItem.toString()) {
                    extraId = extra.workExtraTypeId
                    break
                }
            }
            return WorkExtrasDefinitions(
                cf.generateRandomIdAsLong(),
                curEmployer.employerId,
                extraId,
                cf.getDoubleFromDollarOrPercentString(etValue.text.toString()),
                chkIsFixed.isChecked,
                tvEffectiveDate.text.toString(),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun saveExtra() {
        binding.apply {
            val message = checkExtra()
            if (message == ANSWER_OK) {
                val curExtraDef = getCurExtraDef()
                mainActivity.workExtraViewModel.insertWorkExtraDefinition(
                    curExtraDef
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
        if (mainActivity.mainViewModel.getCallingFragment() != null) {
            if (mainActivity.mainViewModel.getCallingFragment()!!.contains(
                    FRAG_EXTRA_DEFINITIONS
                )
            ) {
                gotoExtraDefinitions()
            }
        } else {
            gotoEmployerUpdate()
        }
    }

    private fun gotoExtraDefinitions() {
        mView.findNavController().navigate(
            EmployerExtraDefinitionsAddFragmentDirections
                .actionEmployerExtraDefinitionsAddFragmentToEmployerExtraDefinitionsFragment()
        )
    }

    private fun gotoEmployerUpdate() {
        mView.findNavController().navigate(
            EmployerExtraDefinitionsAddFragmentDirections
                .actionEmployerExtraDefinitionsAddFragmentToEmployerUpdateFragment()
        )
    }

    private fun checkExtra(): String {
        binding.apply {

            return ANSWER_OK
        }
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

    private fun chooseDate() {
        binding.apply {
            tvEffectiveDate.setOnClickListener {
                changeDate()
            }
        }

    }

    private fun changeDate() {
        binding.apply {
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

    private fun fillValues() {
        binding.apply {
            tvEffectiveDate.text = df.getCurrentDateAsString()
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_250)
                tvEmployer.text = curEmployer.employerName
                if (mainActivity.mainViewModel.getWorkExtraType() != null) {
                    val extraType = mainActivity.mainViewModel.getWorkExtraType()!!
                    for (i in 0 until spExtraTypes.adapter.count) {
                        if (spExtraTypes.getItemAtPosition(i) == extraType.wetName) {
                            spExtraTypes.setSelection(i)
                            break
                        }
                    }
                }
            }
        }
    }

    private fun fillSpinners() {
        curEmployer = mainActivity.mainViewModel.getEmployer()!!
        binding.apply {
            val extraTypeAdapter = ArrayAdapter<String>(
                mView.context,
                R.layout.spinner_item_bold
            )
            mainActivity.workExtraViewModel.getExtraDefTypes(
                curEmployer.employerId
            )
                .observe(viewLifecycleOwner) { typesList ->
                    extraTypeAdapter.clear()
                    extraList.clear()
                    typesList.listIterator().forEach {
                        extraTypeAdapter.add(it.wetName)
                        extraList.add(it)
                    }
                    extraTypeAdapter.add(getString(R.string.add_a_new_extra_type))
                }
            spExtraTypes.adapter = extraTypeAdapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}