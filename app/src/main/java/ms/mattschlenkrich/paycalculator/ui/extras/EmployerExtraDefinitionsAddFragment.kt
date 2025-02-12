package ms.mattschlenkrich.paycalculator.ui.extras

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
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_EXTRA_DEFINITIONS
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.database.model.extras.WorkExtrasDefinitions
import ms.mattschlenkrich.paycalculator.databinding.FragmentEmployerExtraDefinitionsAddBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity


class EmployerExtraDefinitionsAddFragment : Fragment(
    R.layout.fragment_employer_extra_definitions_add
) {

    private var _binding: FragmentEmployerExtraDefinitionsAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var curEmployer: Employers
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private lateinit var extraList: List<WorkExtraTypes>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployerExtraDefinitionsAddBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.add_a_definition)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        populateSpinners()
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
                changeDate()
            }
        }
    }

    private fun populateSpinners() {
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
                    extraList = typesList
                    typesList.listIterator().forEach {
                        extraTypeAdapter.add(it.wetName)
                    }
                    extraTypeAdapter.add(getString(R.string.add_a_new_extra_type))
                }
            spExtraTypes.adapter = extraTypeAdapter
        }
    }

    private fun setClickActions() {
        setMenuActions()
        binding.apply {
            tvEffectiveDate.setOnClickListener {
                changeDate()
            }
        }
        chooseExtraType()
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
            datePickerDialog.setTitle(getString(R.string.choose_when_this_will_take_effect))
            datePickerDialog.show()
        }
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
                            populateFromExtraList()
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        //not needed
                    }
                }
        }
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

    private fun populateFromExtraList() {
        binding.apply {
            for (extra in extraList) {
                if (extra.wetName == spExtraTypes.selectedItem.toString()) {
                    var display = if (extra.wetIsCredit) {
                        getString(R.string.credit)
                    } else {
                        getString(R.string.debit)
                    }
                    display += getString(R.string.__calculated) +
                            resources.getStringArray(
                                R.array.attach_to_frequencies
                            )[extra.wetAppliesTo] +
                            getString(R.string.period_space) +
                            getString(R.string._attaches_to_) +
                            resources.getStringArray(
                                R.array.attach_to_frequencies
                            )[extra.wetAttachTo] +
                            getString(R.string.period_hyphen)
                    display += if (extra.wetIsDefault) getString(R.string.is_automatic)
                    else getString(R.string.added_manually)
                    tvDescription.text = display
                    when (extra.wetAppliesTo) {
                        4 -> {
                            chkIsFixed.isChecked = false
                            chkIsFixed.text = getString(R.string.defaults_to_percentage)
                            chkIsFixed.isEnabled = false
                            etValue.setText(getString(R.string.zero_percent))
                        }

                        1 -> {
                            chkIsFixed.isChecked = true
                            chkIsFixed.text = getString(R.string.defaults_to_fixed)
                            chkIsFixed.isEnabled = false
                            etValue.setText(getString(R.string.zero_dollars))
                        }

                        else -> {
                            chkIsFixed.text = getString(R.string.check_if_this_is_a_fixed_amount)
                            chkIsFixed.isEnabled = true
                        }
                    }
                    break
                }
            }
        }
    }

    private fun getCurrentExtraDefinition(): WorkExtrasDefinitions {
        binding.apply {
            var extraId = 0L
            for (extra in extraList) {
                if (extra.wetName == spExtraTypes.selectedItem.toString()) {
                    extraId = extra.workExtraTypeId
                    break
                }
            }
            return WorkExtrasDefinitions(
                nf.generateRandomIdAsLong(),
                curEmployer.employerId,
                extraId,
                nf.getDoubleFromDollarOrPercentString(etValue.text.toString()),
                chkIsFixed.isChecked,
                tvEffectiveDate.text.toString(),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun saveExtraIfValid() {
        binding.apply {
            val message = validateExtra()
            if (message == ANSWER_OK) {
                saveExtra()
                gotoCallingFragment()
            } else {
                displayError(message)
            }
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
            if (etValue.text.isNullOrBlank() ||
                nf.getDoubleFromDollarOrPercentString(etValue.text.toString()) == 0.0
            ) {
                return getString(R.string.please_enter_a_value_for_this_extra)
            }
            return ANSWER_OK
        }
    }

    private fun saveExtra() {
        val currentExtraDefinition =
            getCurrentExtraDefinition()
        mainActivity.workExtraViewModel.insertWorkExtraDefinition(
            currentExtraDefinition
        )
    }

    private fun gotoCallingFragment() {
        if (mainActivity.mainViewModel.getCallingFragment() != null) {
            if (mainActivity.mainViewModel.getCallingFragment()!!.contains(
                    FRAG_EXTRA_DEFINITIONS
                )
            ) {
                gotoExtraDefinitionsFragment()
            }
        } else {
            gotoEmployerUpdateFragment()
        }
    }

    private fun gotoEmployerUpdateFragment() {
        mView.findNavController().navigate(
            EmployerExtraDefinitionsAddFragmentDirections
                .actionEmployerExtraDefinitionsAddFragmentToEmployerUpdateFragment()
        )
    }

    private fun gotoExtraTypeAdd() {
        mainActivity.mainViewModel.setEmployer(curEmployer)
        gotoWorkExtraTypeAddFragment()
    }

    private fun gotoExtraDefinitionsFragment() {
        mView.findNavController().navigate(
            EmployerExtraDefinitionsAddFragmentDirections
                .actionEmployerExtraDefinitionsAddFragmentToEmployerExtraDefinitionsFragment()
        )
    }

    private fun gotoWorkExtraTypeAddFragment() {
        mView.findNavController().navigate(
            EmployerExtraDefinitionsAddFragmentDirections
                .actionEmployerExtraDefinitionsAddFragmentToWorkExtraTypeAddFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}