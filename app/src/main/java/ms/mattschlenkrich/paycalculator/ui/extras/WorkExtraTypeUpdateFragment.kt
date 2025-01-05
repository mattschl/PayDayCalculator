package ms.mattschlenkrich.paycalculator.ui.extras

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkExtraTypeUpdateBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity

class WorkExtraTypeUpdateFragment : Fragment(
    R.layout.fragment_work_extra_type_update
) {

    private var _binding: FragmentWorkExtraTypeUpdateBinding? = null
    val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()

    //    private val cf = NumberFunctions()
    private val extraTypeList = ArrayList<WorkExtraTypes>()
    private lateinit var currentEmployer: Employers
    private lateinit var currentExtraType: WorkExtraTypes

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkExtraTypeUpdateBinding.inflate(
            layoutInflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title =
            getString(R.string.update_extra_type)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        if (mainActivity.mainViewModel.getEmployer() != null) {
            currentEmployer = mainActivity.mainViewModel.getEmployer()!!
        }
        if (mainActivity.mainViewModel.getWorkExtraType() != null) {
            currentExtraType = mainActivity.mainViewModel.getWorkExtraType()!!
        }
        populateEmployerInfo()
        populateSpinners()
        populateExtraTypeListForValidation()
        populateExtraTypeDetails()
    }

    private fun populateEmployerInfo() {
        binding.apply {
            tvInfo.maxLines = 4
            val display = getString(R.string.update_extra_type_) +
                    currentExtraType.wetName +
                    getString(R.string.__for) +
                    currentEmployer.employerName
            tvInfo.text = display
        }
    }

    private fun populateSpinners() {
        binding.apply {
            val appliesToAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_bold,
                resources.getStringArray(R.array.extra_based_on)
            )
            appliesToAdapter.setDropDownViewResource(R.layout.spinner_item_bold)
            spAppliesTo.adapter = appliesToAdapter
            val attachToAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_bold,
                resources.getStringArray(R.array.pay_per_frequencies)
            )
            attachToAdapter.setDropDownViewResource(R.layout.spinner_item_bold)
            spAttachTo.adapter = attachToAdapter
        }
    }

    private fun populateExtraTypeListForValidation() {
        mainActivity.workExtraViewModel.getExtraDefTypes(currentEmployer.employerId)
            .observe(
                viewLifecycleOwner
            ) { names ->
                extraTypeList.clear()
                names.listIterator().forEach {
                    extraTypeList.add(it)
                }
            }
    }

    private fun populateExtraTypeDetails() {
        binding.apply {
            binding.apply {
                etExtraName.setText(currentExtraType.wetName)
                spAppliesTo.setSelection(currentExtraType.wetAppliesTo)
                spAttachTo.setSelection(currentExtraType.wetAttachTo)
                chkIsCredit.isChecked = currentExtraType.wetIsCredit
                chkIsDefault.isChecked = currentExtraType.wetIsDefault
            }
        }

    }

    private fun setClickActions() {
        setMenuActions()
        onAppliesToSpinnerSelected()
        binding.apply {
            fabDone.setOnClickListener {
                updateExtraTypeIfValid()
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
                        deleteExtraType()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun deleteExtraType() {
        mainActivity.workExtraViewModel.updateWorkExtraType(
            WorkExtraTypes(
                currentExtraType.workExtraTypeId,
                currentExtraType.wetName,
                currentExtraType.wetEmployerId,
                currentExtraType.wetAppliesTo,
                currentExtraType.wetAttachTo,
                currentExtraType.wetIsCredit,
                currentExtraType.wetIsDefault,
                true,
                df.getCurrentTimeAsString()
            )
        )
        gotoCallingFragment()
    }

    private fun onAppliesToSpinnerSelected() {
        binding.apply {
            spAppliesTo.onItemSelectedListener =
                object : OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (position == 4) {
                            spAttachTo.setSelection(3)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        //not needed
                    }
                }
        }
    }

    private fun updateExtraTypeIfValid() {
        val message = validateExtraType()
        if (message == ANSWER_OK) {
            updateExtraTypeAndGotoDefinition()
        } else {
            Toast.makeText(
                mView.context,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun validateExtraType(): String {
        binding.apply {
            var nameFound = false
            var appliesToAllFound = false
            if (extraTypeList.isNotEmpty()) {
                for (extra in extraTypeList) {
                    if (extra.wetName == etExtraName.text.toString().trim() &&
                        extra.wetName != currentExtraType.wetName
                    ) {
                        nameFound = true
                        break
                    }
                    if (extra.wetAppliesTo == 4 &&
                        extra.wetName != etExtraName.text.toString().trim() &&
                        extra.wetName != currentExtraType.wetName
                    ) {
                        appliesToAllFound = true
                        break
                    }
                }
            }
            if (etExtraName.text.isNullOrBlank()) {
                return getString(R.string.error_) +
                        getString(R.string.the_extra_must_have_a_name)
            }
            if (appliesToAllFound) {
                return getString(R.string.error_) +
                        getString(R.string.there_can_only_be_one_extra_that_uses_the_sum_that_includes_other_extras)
            }
            if (nameFound) {
                return getString(R.string.error_) +
                        getString(R.string.this_extra_type_already_exists)
            }
            return ANSWER_OK
        }
    }

    private fun getUpdatedExtraType(): WorkExtraTypes {
        binding.apply {
            return WorkExtraTypes(
                currentExtraType.workExtraTypeId,
                etExtraName.text.toString(),
                currentExtraType.wetEmployerId,
                spAppliesTo.selectedItemPosition,
                spAttachTo.selectedItemPosition,
                chkIsCredit.isChecked,
                chkIsDefault.isChecked,
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun updateExtraTypeAndGotoDefinition() {
        val newExtraType = getUpdatedExtraType()
        mainActivity.workExtraViewModel.updateWorkExtraType(
            newExtraType
        )
        gotoWorkExtraDefinitions(newExtraType)
    }

    private fun gotoWorkExtraDefinitions(newExtraType: WorkExtraTypes) {
        mainActivity.mainViewModel.setWorkExtraType(newExtraType)
        gotoEmployerExtraDefinitionsFragment()
    }

    private fun gotoEmployerExtraDefinitionsFragment() {
        mView.findNavController().navigate(
            WorkExtraTypeUpdateFragmentDirections
                .actionWorkExtraTypeUpdateFragmentToEmployerExtraDefinitionsFragment()
        )
    }

    private fun gotoCallingFragment() {
        gotoWorkExtraDefinitions(currentExtraType)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}