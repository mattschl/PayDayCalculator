package ms.mattschlenkrich.paydaycalculator.ui.extras

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
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.database.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkExtraTypeUpdateBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

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
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (mainActivity.mainViewModel.getEmployer() != null) {
            currentEmployer = mainActivity.mainViewModel.getEmployer()!!
        }
        if (mainActivity.mainViewModel.getWorkExtraType() != null) {
            currentExtraType = mainActivity.mainViewModel.getWorkExtraType()!!
        }
        mainActivity.title =
            "Update ${currentExtraType.wetName} for ${currentEmployer.employerName}"
        populateSpinners()
        getExtraTypeListForValidation()
        setMenuActions()
        onAppliesToSpinnerSelected()
        setClickActions()
        populateValues()
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

    private fun populateValues() {
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
        binding.apply {
            fabDone.setOnClickListener {
                updateExtraType()
            }
        }
    }

    private fun updateExtraType() {
        val message = validateExtraType()
        if (message == ANSWER_OK) {
            val newExtraType = getUpdatedExtraType()
            mainActivity.workExtraViewModel.updateWorkExtraType(
                newExtraType
            )
            gotoWorkExtraDefinitionsFragment(newExtraType)
        } else {
            Toast.makeText(
                mView.context,
                message,
                Toast.LENGTH_LONG
            ).show()
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

    private fun gotoWorkExtraDefinitionsFragment(newExtraType: WorkExtraTypes) {
        mainActivity.mainViewModel.setWorkExtraType(newExtraType)
        mView.findNavController().navigate(
            WorkExtraTypeUpdateFragmentDirections
                .actionWorkExtraTypeUpdateFragmentToEmployerExtraDefinitionsFragment()
        )
    }

    private fun validateExtraType(): String {
        binding.apply {
            var nameFound = false
            var appliesToAllFound = false
            if (extraTypeList.isNotEmpty()) {
                for (extra in extraTypeList) {
                    if (extra.wetName == etExtraName.text.toString().trim() &&
                        extra.wetName != mainActivity.mainViewModel.getWorkExtraType()!!.wetName
                    ) {
                        nameFound = true
                        break
                    }
                    if (extra.wetAppliesTo == 4 &&
                        extra.wetName == mainActivity.mainViewModel.getWorkExtraType()!!.wetName
                    ) {
                        appliesToAllFound = true
                    }
                }
            }
            if (appliesToAllFound) {
                return "    ERROR!!\n" +
                        "There can only be one extra that " +
                        "uses the sum that includes other extras."
            }
            val errorMessage = if (etExtraName.text.isNullOrBlank()) {
                "    ERROR!!\n" +
                        "THe Extra type must have a name"
            } else if (nameFound) {
                "   ERROR!!\n" +
                        "This Extra Type already exists"
            } else {
                ANSWER_OK
            }
            return errorMessage
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

    private fun gotoCallingFragment() {
        gotoWorkExtraDefinitionsFragment(currentExtraType)
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

    private fun getExtraTypeListForValidation() {
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}