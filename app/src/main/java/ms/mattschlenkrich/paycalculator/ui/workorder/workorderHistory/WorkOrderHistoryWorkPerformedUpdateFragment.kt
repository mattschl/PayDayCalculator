package ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.database.model.workorder.Areas
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryWorkPerformedCombined
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkPerformed
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkOrderHistoryWorkPerformedUpdateBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity


class WorkOrderHistoryWorkPerformedUpdateFragment :
    Fragment(R.layout.fragment_work_order_history_work_performed_update) {

    private var _binding: FragmentWorkOrderHistoryWorkPerformedUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var originaWorkOrderlHistory: WorkOrderHistoryWithDates
    private lateinit var originalWorkPerformedHistory: WorkOrderHistoryWorkPerformedCombined
    private lateinit var workPerformedListForAutoComplete: List<WorkPerformed>
    private lateinit var areaListForAutoComplete: List<Areas>
    private var curArea: Areas? = null
    private var curWorkPerformed: WorkPerformed? = null
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkOrderHistoryWorkPerformedUpdateBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.edit_the_work_performed)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateInitialValues()
        setClickActions()
    }

    private fun populateInitialValues() {
        CoroutineScope(Dispatchers.Default).launch {
            populateWorkPerformedObject()
            populateHistoryObject()
            populateWorkPerformedListForAutoComplete()
            populateAreaListForAutoComplete()
            delay(WAIT_250)
            populateUI()
        }
    }

    private suspend fun populateWorkPerformedListForAutoComplete() =
        withContext(Dispatchers.Main) {
            mainActivity.workOrderViewModel.getWorkPerformedAll()
                .observe(viewLifecycleOwner) { list ->
                    workPerformedListForAutoComplete = list
                    val workPerformedStrings = ArrayList<String>()
                    list.listIterator().forEach {
                        workPerformedStrings.add(it.wpDescription)
                    }
                    val wpAdapter = ArrayAdapter(
                        mView.context,
                        R.layout.spinner_item_normal,
                        workPerformedStrings
                    )
                    binding.acWorkPerformed.setAdapter(wpAdapter)
                }
        }


    private suspend fun populateAreaListForAutoComplete() =
        withContext(Dispatchers.Main) {
            mainActivity.workOrderViewModel.getAreasList()
                .observe(viewLifecycleOwner) { list ->
                    areaListForAutoComplete = list
                    val areaNames = ArrayList<String>()
                    list.listIterator().forEachRemaining {
                        areaNames.add(it.areaName)
                    }
                    val areaAdapter = ArrayAdapter(
                        mView.context,
                        R.layout.spinner_item_normal,
                        areaNames
                    )
                    binding.acArea.setAdapter(areaAdapter)
                }
        }

    private suspend fun populateHistoryObject() =
        withContext(Dispatchers.Main) {
            if (mainActivity.mainViewModel.getWorkOrderHistory() != null) {
                mainActivity.workOrderViewModel.getWorkOrderHistoriesById(
                    mainActivity.mainViewModel.getWorkOrderHistory()!!.woHistoryId
                ).observe(viewLifecycleOwner) { history ->
                    originaWorkOrderlHistory = history
                }
            }
        }

    private suspend fun populateWorkPerformedObject() =
        withContext(Dispatchers.Main) {
            if (mainActivity.mainViewModel.getWorkPerformedHistoryId() != null) {
                mainActivity.workOrderViewModel.getWorkPerformedHistoryById(
                    mainActivity.mainViewModel.getWorkPerformedHistoryId()!!
                ).observe(viewLifecycleOwner) { workPerformedHistory ->
                    originalWorkPerformedHistory = workPerformedHistory
                }
            }
        }

    private suspend fun populateUI() =
        withContext(Dispatchers.Main) {
            binding.apply {
                var display = getString(R.string.edit_work_performed_on) +
                        "${originaWorkOrderlHistory.workDate.wdDate}\n" +
                        getString(R.string.for_work_order) +
                        "${originaWorkOrderlHistory.workOrder.woNumber} @ " +
                        "${originaWorkOrderlHistory.workOrder.woAddress} \n " +
                        originaWorkOrderlHistory.workOrder.woDescription
                tvInfo.text = display
                display = getString(R.string.old_work_description) +
                        originalWorkPerformedHistory.workPerformed.wpDescription
                lblWorkPerformed.text = display
                acWorkPerformed.setText(originalWorkPerformedHistory.workPerformed.wpDescription)
                setCurWorkPerformed()
                display = if (originalWorkPerformedHistory.area != null) {
                    getString(R.string.old_area_of_work) +
                            originalWorkPerformedHistory.area!!.areaName
                } else {
                    getString(R.string.no_area_was_indicated)
                }
                lblArea.text = display
                acArea.setText(originalWorkPerformedHistory.area?.areaName)
                setCurArea()
                etWorkPerformedNote.setText(
                    originalWorkPerformedHistory.workOrderHistoryWorkPerformed.wowpNote
                )
            }
        }

    private fun setCurWorkPerformed(): Boolean {
        binding.apply {
            for (workPerformed in workPerformedListForAutoComplete) {
                if (acWorkPerformed.text.toString() ==
                    workPerformed.wpDescription &&
                    !acWorkPerformed.text.isNullOrBlank()
                ) {
                    curWorkPerformed = workPerformed
                    return true
                }
            }
        }
        return false
    }

    private fun setCurArea(): Areas? {
        binding.apply {
            for (area in areaListForAutoComplete) {
                if (acArea.text.toString() ==
                    area.areaName &&
                    !acArea.text.isNullOrBlank()
                ) {
                    curArea = area
                    return area
                }
            }
        }
        return null
    }

    private fun setClickActions() {
//        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}