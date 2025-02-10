package ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory

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
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_UPDATE
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
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
        curWorkPerformed = null
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
        return false
    }

    private fun setCurArea(): Boolean {
        curArea = null
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
        return false
    }

    private fun setClickActions() {
        binding.apply {
            fabDone.setOnClickListener { updateWorkPerformedInHistoryIfValid() }
        }
    }

    private fun updateWorkPerformedInHistoryIfValid() {
        val message = validateOrAddWorkPerformedToDbAndUpdateWithArea()
        if (message != ANSWER_OK) {
            Toast.makeText(
                mView.context,
                getString(R.string.error_) +
                        message,
                Toast.LENGTH_LONG
            ).show()
        } else {
//            gotoCallingFragment()
        }
    }

    private fun validateOrAddWorkPerformedToDbAndUpdateWithArea(): String {
        binding.apply {
            if (setCurWorkPerformed()) {
                addAreaAndUpdateWorkPerformedHistory(curWorkPerformed!!)
                return ANSWER_OK
            } else if (!acWorkPerformed.text.isNullOrBlank()) {
                insertWorkPerformedIntoDbAndContinueToUpdate()
                return ANSWER_OK
            }
        }
        return getString(R.string.please_enter_a_valid_description_of_work_performed_to_add_it)
    }

    private fun insertWorkPerformedIntoDbAndContinueToUpdate() {
        CoroutineScope(Dispatchers.Default).launch {
            val workPerformed = insertWorkPerformedIntoDatabase(
                binding.acWorkPerformed.text.toString().trim()
            )
            addAreaAndUpdateWorkPerformedHistory(workPerformed)
        }
    }

    private fun addAreaAndUpdateWorkPerformedHistory(workPerformed: WorkPerformed) {
        CoroutineScope(Dispatchers.Main).launch {
            binding.apply {
                if (acArea.text.isNullOrBlank()) {
                    curArea = null
                    updateHistory(workPerformed.workPerformedId, null)
                } else if (setCurArea()) {
                    updateHistory(workPerformed.workPerformedId, curArea?.areaId)
                } else if (!acArea.text.isNullOrBlank()) {
                    val newAreaId = async { insertAreaIntoDb(acArea.text.toString().trim()) }
                    updateHistory(workPerformed.workPerformedId, newAreaId.await())
                }
            }
        }
    }

    private fun updateHistory(workPerformedId: Long, areaId: Long?) {
        CoroutineScope(Dispatchers.Main).launch {
            val note: String? = getNote()
            delay(WAIT_250)
            mainActivity.workOrderViewModel.updateWorkOrderHistoryWorkPerformed(
                WorkOrderHistoryWorkPerformed(
                    originalWorkPerformedHistory.workOrderHistoryWorkPerformed.workOrderHistoryWorkPerformedId,
                    originalWorkPerformedHistory.workOrderHistoryWorkPerformed.wowpHistoryId,
                    workPerformedId,
                    areaId,
                    note,
                    originalWorkPerformedHistory.workOrderHistoryWorkPerformed.wowpSequence,
                    false,
                    df.getCurrentTimeAsString()
                )
            )
            gotoCallingFragment()
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

    private fun insertAreaIntoDb(areaName: String): Long {
        val newArea = Areas(
            nf.generateRandomIdAsLong(),
            areaName,
            false,
            df.getCurrentTimeAsString()
        )
        mainActivity.workOrderViewModel.insertArea(newArea)
        return newArea.areaId
    }

    private fun insertWorkPerformedIntoDatabase(workPerformedName: String): WorkPerformed {
        val newWorkPerformed = WorkPerformed(
            nf.generateRandomIdAsLong(),
            workPerformedName,
            false,
            df.getCurrentTimeAsString()
        )
        mainActivity.workOrderViewModel.insertWorkPerformed(newWorkPerformed)
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
        mainActivity.mainViewModel.setWorkPerformedHistoryId(null)
        mainActivity.mainViewModel.setWorkPerformedId(null)
        mainActivity.mainViewModel.setAreaId(null)
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