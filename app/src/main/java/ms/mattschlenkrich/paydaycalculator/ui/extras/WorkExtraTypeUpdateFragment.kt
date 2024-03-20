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
import ms.mattschlenkrich.paydaycalculator.common.CommonFunctions
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkExtraTypeUpdateBinding
import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.model.WorkExtraTypes
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class WorkExtraTypeUpdateFragment : Fragment(
    R.layout.fragment_work_extra_type_update
) {

    var _binding: FragmentWorkExtraTypeUpdateBinding? = null
    val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()
    private val cf = CommonFunctions()
    private val extraTypeList = ArrayList<WorkExtraTypes>()
    private lateinit var curEmployer: Employers
    private lateinit var curExtraType: WorkExtraTypes

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
            curEmployer = mainActivity.mainViewModel.getEmployer()!!
        }
        if (mainActivity.mainViewModel.getWorkExtraType() != null) {
            curExtraType = mainActivity.mainViewModel.getWorkExtraType()!!
        }
        mainActivity.title = "Update ${curExtraType.wetName} for ${curEmployer.employerName}"
        fillSpinners()
        getExtraTypeList()
        fillMenu()
        setActions()
        fillValues()
    }

    private fun fillValues() {
        binding.apply {
            binding.apply {
                etExtraName.setText(curExtraType.wetName)
                spAppliesTo.setSelection(curExtraType.wetAppliesTo)
                spAttachTo.setSelection(curExtraType.wetAttachTo)
                chkIsCredit.isChecked = curExtraType.wetIsCredit
                chkIsDefault.isChecked = curExtraType.wetIsDefault
            }
        }
    }

    private fun setActions() {
        binding.apply {
            fabDone.setOnClickListener {
                updateExtraType()
            }
        }
    }

    private fun updateExtraType() {
        binding.apply {
            val message = checkExtraType()
            if (message == ANSWER_OK) {
                val newExtraType =
                    WorkExtraTypes(
                        curExtraType.workExtraTypeId,
                        etExtraName.text.toString(),
                        curExtraType.wetEmployerId,
                        spAppliesTo.selectedItemPosition,
                        spAttachTo.selectedItemPosition,
                        chkIsCredit.isChecked,
                        chkIsDefault.isChecked,
                        false,
                        df.getCurrentTimeAsString()
                    )
                mainActivity.workExtraViewModel.updateWorkExtraType(
                    newExtraType
                )
                gotoWorkExtraDefinitions(newExtraType)
            } else {
                Toast.makeText(
                    mView.context,
                    message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun gotoWorkExtraDefinitions(newExtraType: WorkExtraTypes) {
        mainActivity.mainViewModel.setWorkExtraType(newExtraType)
        mView.findNavController().navigate(
            WorkExtraTypeUpdateFragmentDirections
                .actionWorkExtraTypeUpdateFragmentToEmployerExtraDefinitionsFragment()
        )
    }

    private fun checkExtraType(): String {
        binding.apply {
            var nameFound = false
            if (extraTypeList.isNotEmpty()) {
                for (extra in extraTypeList) {
                    if (extra.wetName == etExtraName.text.toString().trim() &&
                        extra.wetName != curExtraType.wetName
                    ) {
                        nameFound = true
                        break
                    }
                }
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

    private fun fillMenu() {
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
                curExtraType.workExtraTypeId,
                curExtraType.wetName,
                curExtraType.wetEmployerId,
                curExtraType.wetAppliesTo,
                curExtraType.wetAttachTo,
                curExtraType.wetIsCredit,
                curExtraType.wetIsDefault,
                true,
                df.getCurrentTimeAsString()
            )
        )
        gotoCallingFragment()
    }

    private fun gotoCallingFragment() {
        gotoWorkExtraDefinitions(curExtraType)
    }

    private fun fillSpinners() {
        binding.apply {
            val frequencyAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_bold,
                resources.getStringArray(R.array.pay_per_frequencies)
            )
            frequencyAdapter.setDropDownViewResource(R.layout.spinner_item_bold)
            spAppliesTo.adapter = frequencyAdapter
            spAttachTo.adapter = frequencyAdapter
        }
    }

    private fun getExtraTypeList() {
        mainActivity.workExtraViewModel.getExtraDefTypes(curEmployer.employerId)
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