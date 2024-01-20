package ms.mattschlenkrich.paydaycalculator.ui.paydays

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.WAIT_250
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkDateAddBinding
import ms.mattschlenkrich.paydaycalculator.model.PayPeriods
import ms.mattschlenkrich.paydaycalculator.model.WorkDates
import java.time.LocalDate

private const val TAG = "WorkDateAdd"

class WorkDateAddFragment : Fragment(R.layout.fragment_work_date_add) {

    private var _binding: FragmentWorkDateAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var curDate: String
    private var payPeriod: PayPeriods? = null
    private val df = DateFunctions()

    //    private val cf = CommonFunctions()
    private val usedWorkDatesList = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkDateAddBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.add_a_new_work_date)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fillValues()
        fillMenu()
        fillExtras()
        selectDate()
        setActions()
    }

    private fun setActions() {
        binding.apply {
            fabAddExtra.setOnClickListener {
                addExtra()
            }
        }
    }

    private fun addExtra() {
        mainActivity.mainViewModel.setWorkDateObject(getCurWorkDate())
        mainActivity.mainViewModel.setCallingFragment(TAG)
        mView.findNavController().navigate(
            WorkDateAddFragmentDirections
                .actionGlobalEmployerExtraDefinitionsFragment()
        )
    }

    private fun selectDate() {
        binding.apply {
            tvWorkDate.setOnClickListener {
                val curDateAll = curDate.split("-")
                val datePickerDialog = DatePickerDialog(
                    requireContext(),
                    { _, year, monthOfYear, dayOfMonth ->
                        val month = monthOfYear + 1
                        val display = "$year-${
                            month.toString()
                                .padStart(2, '0')
                        }-${
                            dayOfMonth.toString().padStart(2, '0')
                        }"
                        curDate = display
                        tvWorkDate.text = df.getDisplayDate(curDate)

                    },
                    curDateAll[0].toInt(),
                    curDateAll[1].toInt() - 1,
                    curDateAll[2].toInt()
                )
                datePickerDialog.setTitle(getString(R.string.choose_a_work_date))
                datePickerDialog.show()
            }
        }
    }

    private fun fillValues() {
        if (mainActivity.mainViewModel.getPayPeriod() != null) {
            payPeriod = mainActivity.mainViewModel.getPayPeriod()
            mainActivity.payDayViewModel.getWorkDateList(
                payPeriod!!.ppEmployerId,
                payPeriod!!.ppCutoffDate
            ).observe(viewLifecycleOwner) { list ->
                usedWorkDatesList.clear()
                list.listIterator().forEach {
                    usedWorkDatesList.add(it.wdDate)
                }
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_250)
                fillDate()
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
                        checkSaveWorkDate()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun gotoCallingFragment() {
        mView.findNavController().navigate(
            WorkDateAddFragmentDirections
                .actionWorkDateAddFragmentToTimeSheetFragment()
        )
    }

    private fun checkSaveWorkDate() {
        if (usedWorkDatesList.isEmpty()) {
            saveWorkDate()
        } else {
            for (date in usedWorkDatesList) {
                if (date == curDate) {
                    AlertDialog.Builder(mView.context)
                        .setTitle("This date is already used")
                        .setMessage(
                            "Would you like to REPLACE the old information for " +
                                    "this work date?"
                        )
                        .setPositiveButton("Yes") { _, _ ->
                            saveWorkDate()
                        }
                        .setNegativeButton("No", null)
                        .show()
                } else {
                    saveWorkDate()
                }
            }
        }
    }

    private fun saveWorkDate() {
        mainActivity.payDayViewModel.insertWorkDate(getCurWorkDate())
        gotoCallingFragment()
    }

    private fun fillDate() {
        curDate = LocalDate.now().toString()
        for (date in usedWorkDatesList) {
            if (curDate == date) {
                curDate = LocalDate.parse(curDate).plusDays(1L).toString()
            }
        }
        binding.tvWorkDate.text = df.getDisplayDate(curDate)
    }

    private fun fillExtras() {
        binding.apply {

        }
    }

    private fun getCurWorkDate(): WorkDates {
        binding.apply {
            return WorkDates(
                payPeriod!!.ppEmployerId,
                payPeriod!!.ppCutoffDate,
                curDate,
                if (etHours.text.isNullOrBlank()) 0.0 else etHours.text.toString().trim()
                    .toDouble(),
                if (etOt.text.isNullOrBlank()) 0.0 else etOt.text.toString().trim()
                    .toDouble(),
                if (etDblOt.text.isNullOrBlank()) 0.0 else etDblOt.text.toString().trim()
                    .toDouble(),
                if (etStat.text.isNullOrBlank()) 0.0 else etStat.text.toString().trim()
                    .toDouble(),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}