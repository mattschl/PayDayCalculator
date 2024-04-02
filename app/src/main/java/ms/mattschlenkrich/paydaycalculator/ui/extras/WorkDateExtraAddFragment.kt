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
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.MoneyFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkDateExtraAddBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkDateExtras
import ms.mattschlenkrich.paydaycalculator.model.WorkDates
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class WorkDateExtraAddFragment : Fragment(R.layout.fragment_work_date_extra_add) {

    private var _binding: FragmentWorkDateExtraAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var curDateObject: WorkDates
    private var extraList = ArrayList<WorkDateExtras>()
    private val df = DateFunctions()
    private val cf = MoneyFunctions()


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
        fillMenu()
        fillValues()
        chooseFixedOrPercent()
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
            getDateExtraList()
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

    private fun getDateExtraList() {
        if (mainActivity.mainViewModel.getWorkDateExtraList().isNotEmpty()) {
            extraList = mainActivity.mainViewModel.getWorkDateExtraList()
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
        val message = checkExtra()
        if (message == ANSWER_OK) {
            mainActivity.payDayViewModel.insertWorkDateExtra(
                getCurExtra()
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

    private fun gotoCallingFragment() {
        mainActivity.mainViewModel.eraseWorkDateExtraList()
        mView.findNavController().navigate(
            WorkDateExtraAddFragmentDirections
                .actionWorkDateExtraAddFragmentToWorkDateUpdateFragment()
        )
    }

    private fun getCurExtra(): WorkDateExtras {
        binding.apply {
            return WorkDateExtras(
                cf.generateId(),
                curDateObject.workDateId,
                null,
                etExtraName.text.toString(),
                spAppliesTo.selectedItemPosition,
                1,
                cf.getDoubleFromDollarOrPercent(etValue.text.toString()),
                chkIsCredit.isChecked,
                chkIsFixed.isChecked,
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun checkExtra(): String {
        binding.apply {
            var nameFound = false
            if (extraList.isNotEmpty()) {
                for (extra in extraList) {
                    if (extra.wdeName == etExtraName.text.toString().trim()) {
                        nameFound = true
                        break
                    }
                }
            }
            val errorMessage = if (etExtraName.text.isNullOrBlank()) {
                "    ERROR!!\n" +
                        "The Extra must have a name"
            } else if (nameFound) {
                "   ERROR!!\n" +
                        "This Extra name has already been used. \n" +
                        "Choose a different name."
            } else if (cf.getDoubleFromDollarOrPercent(etValue.text.toString()) == 0.0) {
                "   ERROR!!\n" +
                        "This Extra must have a value"
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