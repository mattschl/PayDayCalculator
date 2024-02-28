package ms.mattschlenkrich.paydaycalculator.ui.extras

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
import ms.mattschlenkrich.paydaycalculator.common.CommonFunctions
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkDateExtraUpdateBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkDateExtras
import ms.mattschlenkrich.paydaycalculator.model.WorkDates

class WorkDateExtraUpdateFragment : Fragment(R.layout.fragment_work_date_extra_update) {

    private var _binding: FragmentWorkDateExtraUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var curDateObject: WorkDates
    private lateinit var curWorkDateExtra: WorkDateExtras
    private var extraList = ArrayList<WorkDateExtras>()
    private val df = DateFunctions()
    private val cf = CommonFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkDateExtraUpdateBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = "Update this Extra"
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fillSpinners()
        chooseFixedOrPercent()
        fillMenu()
        fillValues()
        setActions()
    }

    private fun setActions() {
        binding.apply {
            fabDone.setOnClickListener {
                updateWorkDateExtra()
            }
        }
    }

    private fun updateWorkDateExtra() {
        TODO("Not yet implemented")
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
        if (mainActivity.mainViewModel.getWorkDateObject() != null &&
            mainActivity.mainViewModel.getWorkDateExtra() != null &&
            mainActivity.mainViewModel.getWorkDateString() != null
        ) {
            curDateObject =
                mainActivity.mainViewModel.getWorkDateObject()!!
            curWorkDateExtra =
                mainActivity.mainViewModel.getWorkDateExtra()!!
            mainActivity.title = "Update Extra: ${curWorkDateExtra.wdeName}"
            binding.apply {
                var display = "Date: ${
                    df.getDisplayDate(
                        mainActivity.mainViewModel.getWorkDateString()!!
                    )
                } " +
                        "Employer: ${mainActivity.mainViewModel.getEmployerString()}"
                lblDateInfo.text = display
                etExtraName.setText(curWorkDateExtra.wdeName)
                spAppliesTo.setSelection(curWorkDateExtra.wdeAppliesTo)
                display = if (curWorkDateExtra.wdeIsFixed) {
                    cf.displayDollars(curWorkDateExtra.wdeValue)
                } else {
                    cf.displayPercentFromDouble(curWorkDateExtra.wdeValue)
                }
                etValue.setText(display)
                chkIsFixed.isChecked = curWorkDateExtra.wdeIsFixed
                chkIsCredit.isChecked = curWorkDateExtra.wdeIsCredit
            }
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
                        deleteExtra()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun deleteExtra() {
        //todo create the action
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}