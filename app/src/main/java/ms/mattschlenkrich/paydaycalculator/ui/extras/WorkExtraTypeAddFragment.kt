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
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.database.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkExtraTypeAddBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class WorkExtraTypeAddFragment : Fragment(
    R.layout.fragment_work_extra_type_add
) {

    private var _binding: FragmentWorkExtraTypeAddBinding? = null
    val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()
    private val cf = NumberFunctions()
    private val extraTypeList = ArrayList<WorkExtraTypes>()
    private lateinit var curEmployer: Employers

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkExtraTypeAddBinding.inflate(
            inflater, container, false
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
        mainActivity.title = "Add Extra Type for ${curEmployer.employerName}"
        setClickActions()
        populateValues()
    }

    private fun populateValues() {
        populateSpinners()
        populateExtraTypeList()
    }

    private fun setClickActions() {
        setMenuActions()
        onAppliesToSpinnerSelected()
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

    private fun populateExtraTypeList() {
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

    private fun setMenuActions() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.save_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_save -> {
                        saveExtraType()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun saveExtraType() {
        val message = validateExtraType()
        if (message == ANSWER_OK) {
            val newWorkExtraType = getNewWorkExtraType()
            mainActivity.workExtraViewModel.insertWorkExtraType(newWorkExtraType)
            gotoNextStep(newWorkExtraType)
        } else {
            Toast.makeText(
                mView.context,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun getNewWorkExtraType(): WorkExtraTypes {
        binding.apply {
            return WorkExtraTypes(
                cf.generateRandomIdAsLong(),
                etExtraName.text.toString(),
                curEmployer.employerId,
                spAppliesTo.selectedItemPosition,
                spAttachTo.selectedItemPosition,
                chkIsCredit.isChecked,
                chkIsDefault.isChecked,
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun gotoNextStep(extraType: WorkExtraTypes) {
        mainActivity.mainViewModel.setEmployer(curEmployer)
        mainActivity.mainViewModel.setWorkExtraType(extraType)
        mView.findNavController().navigate(
            WorkExtraTypeAddFragmentDirections
                .actionWorkExtraTypeAddFragmentToEmployerExtraDefinitionsFragment()
        )
    }

    private fun validateExtraType(): String {
        binding.apply {
            var nameFound = false
            var appliesToAllFound = false
            if (extraTypeList.isNotEmpty()) {
                for (extra in extraTypeList) {
                    if (extra.wetName == etExtraName.text.toString().trim()) {
                        nameFound = true
                        break
                    }
                    if (extra.wetAppliesTo == 4 &&
                        extra.wetName != etExtraName.text.toString().trim()
                    ) {
                        appliesToAllFound = true
                        break
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}