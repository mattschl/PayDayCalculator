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
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkExtraTypeAddBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity

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
        mainActivity.title = getString(R.string.add_extra_type)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickActions()
        populateValues()
    }

    private fun populateValues() {
        if (mainActivity.mainViewModel.getEmployer() != null) {
            curEmployer = mainActivity.mainViewModel.getEmployer()!!
        }
        populateEmployerInfo()
        populateSpinners()
        populateExtraTypeList()
    }

    private fun populateEmployerInfo() {
        binding.apply {
            tvInfo.maxLines = 4
            val display = getString(R.string.add_a_new_extra_type) +
                    getString(R.string._for_) +
                    curEmployer.employerName
            tvInfo.text = display
        }
    }

    private fun populateSpinners() {
        binding.apply {
            val appliesToAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_bold,
                resources.getStringArray(R.array.applies_to_frequencies)
            )
            appliesToAdapter.setDropDownViewResource(R.layout.spinner_item_bold)
            spAppliesTo.adapter = appliesToAdapter
            val attachToAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_bold,
                resources.getStringArray(R.array.attach_to_frequencies)
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

    private fun setClickActions() {
        setMenuActions()
        onAppliesToSpinnerSelected()
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
                        saveExtraTypeIfValid()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
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

    private fun saveExtraTypeIfValid() {
        val message = validateExtraType()
        if (message == ANSWER_OK) {
            saveExtraTypeAndGotoDefinition()
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

    private fun getCurrentWorkExtraType(): WorkExtraTypes {
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

    private fun saveExtraTypeAndGotoDefinition() {
        val newWorkExtraType = getCurrentWorkExtraType()
        mainActivity.workExtraViewModel.insertWorkExtraType(newWorkExtraType)
        gotoEmployerExtraDefinitions(newWorkExtraType)
    }

    private fun gotoEmployerExtraDefinitions(extraType: WorkExtraTypes) {
        mainActivity.mainViewModel.setEmployer(curEmployer)
        mainActivity.mainViewModel.setWorkExtraType(extraType)
        gotoEmployerExtraDefinitionsFragment()
    }

    private fun gotoEmployerExtraDefinitionsFragment() {
        mView.findNavController().navigate(
            WorkExtraTypeAddFragmentDirections
                .actionWorkExtraTypeAddFragmentToEmployerExtraDefinitionsFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}