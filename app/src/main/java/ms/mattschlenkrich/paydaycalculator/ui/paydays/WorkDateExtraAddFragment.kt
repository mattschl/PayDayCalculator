package ms.mattschlenkrich.paydaycalculator.ui.paydays

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.CommonFunctions
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkDateExtraAddBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkDates
import ms.mattschlenkrich.paydaycalculator.model.WorkExtraTypes

class WorkDateExtraAddFragment : Fragment(R.layout.fragment_work_date_extra_add) {

    private var _binding: FragmentWorkDateExtraAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var curDateObject: WorkDates
    private val extraTypeList = ArrayList<WorkExtraTypes>()
    private val df = DateFunctions()
    private val cf = CommonFunctions()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkDateExtraAddBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = "Add a one time extra"
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fillSpinners()
        fillValues()
        fillMenu()
        getExtraTypeList()
    }

    private fun fillValues() {
        if (mainActivity.mainViewModel.getWorkDateObject() != null) {
            curDateObject =
                mainActivity.mainViewModel.getWorkDateObject()!!
            var curEmployerString = ""
            mainActivity.employerViewModel.getEmployer(curDateObject.wdEmployerId)
                .observe(viewLifecycleOwner) { employer ->
                    curEmployerString = employer.employerName
                    val display = "Date: " +
                            "${df.getDisplayDate(curDateObject.wdDate)} " +
                            "Employer: $curEmployerString"
                    binding.lblDateInfo.text = display
                }
            val display = "Date: ${df.getDisplayDate(curDateObject.wdDate)} " +
                    "Employer: $curEmployerString"
            binding.lblDateInfo.text = display
        }
    }

    private fun fillSpinners() {
        binding.apply {
            val frequencies = ArrayList<String>()
            for (i in 0..1) {
                frequencies.add(
                    resources.getStringArray(R.array.pay_per_frequencies)[i]
                )
            }
            val frequencyAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_bold,
                frequencies
            )
            frequencyAdapter.setDropDownViewResource(R.layout.spinner_item_bold)
            spAppliesTo.adapter = frequencyAdapter
        }
    }

    private fun getExtraTypeList() {
        mainActivity.workExtraViewModel.getExtraDefTypes(curDateObject.wdEmployerId)
            .observe(
                viewLifecycleOwner
            ) { names ->
                extraTypeList.clear()
                names.listIterator().forEach {
                    extraTypeList.add(it)
                }
            }
    }

    private fun fillMenu() {
        mainActivity.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.save_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_save -> {
                        checkSaveWorkDateExtra()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun checkSaveWorkDateExtra() {
        if (checkExtraType() == ANSWER_OK) {
            mainActivity
        }
    }

    private fun checkExtraType(): String {
        binding.apply {
            var nameFound = false
            if (extraTypeList.isNotEmpty()) {
                for (extra in extraTypeList) {
                    if (extra.wetName == etExtraName.text.toString().trim()) {
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}