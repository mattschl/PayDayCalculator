package ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory

import android.app.AlertDialog
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_UPDATE
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_100
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.database.model.workorder.Areas
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryWorkPerformed
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
    private lateinit var originalWorkOrderHistory: WorkOrderHistoryWithDates
    private lateinit var originalWorkPerformedHistory: WorkOrderHistoryWorkPerformedCombined
    private lateinit var workPerformedListForAutoComplete: List<WorkPerformed>
    private lateinit var areaListForAutoComplete: List<Areas>
    private lateinit var historyWorkPerformedCombinedList:
            List<WorkOrderHistoryWorkPerformedCombined>
    private var curArea: Areas? = null
    private var curWorkPerformed: WorkPerformed? = null
    private val nf = NumberFunctions()
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
            delay(WAIT_100)
            populateHistoryObject()
            delay(WAIT_100)
            populateWorkPerformedListForAutoComplete()
            populateAreaListForAutoComplete()
            populateHistoryWorkPerformedList()
            delay(WAIT_250)
            populateUI()
        }
    }

    private suspend fun populateHistoryWorkPerformedList() =
        withContext(Dispatchers.Main) {
            mainActivity.workOrderViewModel.getWorkPerformedCombinedByWorkOrderHistory(
                originalWorkOrderHistory.history.woHistoryId
            ).observe(viewLifecycleOwner) { list ->
                historyWorkPerformedCombinedList = list
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
                    originalWorkOrderHistory = history
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
                        "${originalWorkOrderHistory.workDate.wdDate}\n" +
                        getString(R.string.for_work_order) +
                        "${originalWorkOrderHistory.workOrder.woNumber} @ " +
                        "${originalWorkOrderHistory.workOrder.woAddress} \n " +
                        originalWorkOrderHistory.workOrder.woDescription
                tvInfo.text = display
                display = getString(R.string.old_work_description) +
                        originalWorkPerformedHistory.workPerformed.wpDescription
                lblWorkPerformed.text = display
                acWorkPerformed.setText(originalWorkPerformedHistory.workPerformed.wpDescription)
                display = if (originalWorkPerformedHistory.area != null) {
                    getString(R.string.old_area_of_work) +
                            originalWorkPerformedHistory.area!!.areaName
                } else {
                    getString(R.string.no_area_was_indicated)
                }
                lblArea.text = display
                acArea.setText(originalWorkPerformedHistory.area?.areaName)
                if (originalWorkPerformedHistory.workOrderHistoryWorkPerformed.wowpNote != null) {
                    etWorkPerformedNote.setText(
                        originalWorkPerformedHistory.workOrderHistoryWorkPerformed.wowpNote
                    )
                }
            }
        }

    private fun setCurWorkPerformed(): Boolean {
        binding.apply {
            if (!acWorkPerformed.text.isNullOrBlank()) {
                for (workPerformed in workPerformedListForAutoComplete) {
                    if (acWorkPerformed.text.toString().trim() ==
                        workPerformed.wpDescription
                    ) {
                        curWorkPerformed = workPerformed
                        return true
                    }
                }
            }
        }
        curWorkPerformed = null
        return false
    }

    private fun setCurArea(): Boolean {
        binding.apply {
            if (!acArea.text.isNullOrBlank()) {
                for (area in areaListForAutoComplete) {
                    if (acArea.text.toString().trim() ==
                        area.areaName
                    ) {
                        curArea = area
                        return true
                    }
                }
            }
        }
        curArea = null
        return false
    }

    private fun setClickActions() {
        binding.apply {
            fabDone.setOnClickListener { updateWorkPerformedInHistoryIfValid() }
        }
    }

    private fun updateWorkPerformedInHistoryIfValid() {
        binding.apply {
            CoroutineScope(Dispatchers.Main).launch {
                if (acWorkPerformed.text.isNullOrBlank()) {
                    displayMessage(
                        getString(R.string.error_) +
                                getString(R.string.please_enter_a_valid_work_performed_description)
                    )
                } else {
                    val workPerformed = async {
                        return@async if (setCurWorkPerformed()) {
                            curWorkPerformed
                        } else {
                            insertWorkPerformedIntoDatabase(
                                acWorkPerformed.text.toString().trim()
                            )
                        }
                    }
                    val area = async {
                        return@async if (setCurArea()) {
                            curArea!!
                        } else if (acArea.text.isNullOrBlank()) {
                            null
                        } else {
                            insertAreaIntoDb(
                                acArea.text.toString().trim()
                            )
                        }
                    }
                    val combinedWorkPerformedIsUnique = async {
                        isCombinedWorkPerformedUnique(
                            workPerformed.await()!!, area.await()
                        )
                    }
                    if (combinedWorkPerformedIsUnique.await()) {
                        updateWorkHistory(
                            workPerformed.await()!!.workPerformedId,
                            area.await()?.areaId
                        )
                        gotoCallingFragment()
                    } else {
                        displayMessage(
                            getString(R.string.error_) +
                                    getString(R.string.this_work_performed_and_area_combination_is_already_in_this_work_history)
                        )
                    }
                }
            }
        }
    }

    private fun isCombinedWorkPerformedUnique(
        workPerformed: WorkPerformed, area: Areas?
    ): Boolean {
        for (combinedWorkPerformed in historyWorkPerformedCombinedList) {
            if (workPerformed.workPerformedId == combinedWorkPerformed.workPerformed.workPerformedId &&
                area?.areaId == combinedWorkPerformed.area?.areaId
            ) {
                return false
            }
        }
        return true
    }

    private fun displayMessage(message: String) {
        Toast.makeText(
            mView.context,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun updateWorkHistory(workPerformedId: Long, areaId: Long?) {
        CoroutineScope(Dispatchers.Main).launch {
            val note: String? = getNote()
            delay(WAIT_250)
            try {
                originalWorkPerformedHistory.workOrderHistoryWorkPerformed.apply {
                    mainActivity.workOrderViewModel.updateWorkOrderHistoryWorkPerformed(
                        WorkOrderHistoryWorkPerformed(
                            workOrderHistoryWorkPerformedId,
                            wowpHistoryId,
                            workPerformedId,
                            areaId,
                            note,
                            wowpSequence,
                            false,
                            df.getCurrentTimeAsString()
                        )
                    )
                }
            } catch (e: SQLiteException) {
                AlertDialog.Builder(mView.context)
                    .setTitle(getString(R.string.something_went_wrong))
                    .setMessage(
                        getString(R.string.check_to_see_if_this_work_was_already_entered_) +
                                " " + e.toString()
                    )
                    .setNeutralButton(getString(R.string.ok), null)
                    .show()
            }
        }
    }

    private fun getNote(): String? {
        binding.apply {
            return if (etWorkPerformedNote.text.isNullOrBlank()) {
                null
            } else {
                etWorkPerformedNote.text.toString().trim()
            }
        }
    }

    private fun insertAreaIntoDb(areaName: String?): Areas? {
        if (areaName != null) {
            val newArea = Areas(
                nf.generateRandomIdAsLong(),
                areaName,
                false,
                df.getCurrentTimeAsString()
            )
            try {
                mainActivity.workOrderViewModel.insertArea(newArea)
                return newArea
            } catch (e: SQLiteException) {
                AlertDialog.Builder(mView.context)
                    .setTitle(getString(R.string.something_went_wrong))
                    .setMessage(
                        getString(R.string.check_to_see_if_this_work_was_already_entered_) +
                                " " + e.toString()
                    )
                    .setNeutralButton(getString(R.string.ok), null)
                    .show()
                return null
            }
        } else {
            return null
        }
    }

    private fun insertWorkPerformedIntoDatabase(workPerformedName: String): WorkPerformed? {
        val newWorkPerformed = WorkPerformed(
            nf.generateRandomIdAsLong(),
            workPerformedName,
            false,
            df.getCurrentTimeAsString()
        )
        try {
            mainActivity.workOrderViewModel.insertWorkPerformed(newWorkPerformed)
        } catch (e: SQLiteException) {
            return null
        }
        return newWorkPerformed
    }

    private fun gotoCallingFragment() {
        if (mainActivity.mainViewModel.getCallingFragment()!!.contains(
                FRAG_WORK_ORDER_HISTORY_UPDATE
            )
        ) {
            gotoWorkOrderHistoryUpdate()
        }
    }

    private fun gotoWorkOrderHistoryUpdate() {
        mainActivity.mainViewModel.apply {
            setWorkPerformedHistoryId(null)
            setWorkPerformedId(null)
            setAreaId(null)
        }
        gotoWorkOrderHistoryUpdateFragment()
    }

    private fun gotoWorkOrderHistoryUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryWorkPerformedUpdateFragmentDirections
                .actionWorkOrderHistoryWorkPerformedUpdateFragmentToWorkOrderHistoryUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}