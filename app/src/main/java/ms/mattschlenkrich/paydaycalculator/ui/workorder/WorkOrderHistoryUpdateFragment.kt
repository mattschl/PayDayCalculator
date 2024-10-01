package ms.mattschlenkrich.paydaycalculator.ui.workorder

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.adapter.workorders.WorKOrderHistoryWorkPerformedAdapter
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.FRAG_WORK_ODER_HISTORY_UPDATE
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.common.WAIT_250
import ms.mattschlenkrich.paydaycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.TempWorkOrderHistoryInfo
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrder
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrderHistory
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrderHistoryWorkPerformed
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrderHistoryWorkPerformedCombined
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkPerformed
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkPerformedInSequence
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkOrderHistoryBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

private const val TAG = FRAG_WORK_ODER_HISTORY_UPDATE

class WorkOrderHistoryUpdateFragment : Fragment(R.layout.fragment_work_order_history) {

    private var _binding: FragmentWorkOrderHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private lateinit var workOrderList: ArrayList<String>
    private lateinit var workDateObject: WorkDates
    private lateinit var curEmployer: Employers
    private lateinit var curHistory: WorkOrderHistoryWithDates
    private lateinit var curWorkOrder: WorkOrder
    private var workPerformedListForAutoComplete =
        ArrayList<WorkPerformed>()
    private var curWorkPerformed: WorkPerformed? = null
    private var workPerformedSequence = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkOrderHistoryBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.add_time_to_work_order)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateInitialValues()
        setClickActions()
    }

    private fun onSelectWorkOrder() {
        binding.apply {
            acWorkOrder.setOnItemClickListener { _, _, _, _ ->
                mainActivity.workOrderViewModel.getWorkOrder(
                    acWorkOrder.text.toString()
                ).observe(viewLifecycleOwner) { workOrder ->
                    curWorkOrder = workOrder
                    displayWorkOrderInfo(workOrder)
                }
            }
        }
    }

    private fun displayWorkOrderInfo(workOrder: WorkOrder) {
        val display = workOrder.woAddress +
                " | " + workOrder.woDescription
        binding.apply {
            tvDescription.text = display
            tvDescription.visibility = View.VISIBLE
            btnEditWorkOrder.visibility = View.VISIBLE
        }
    }

    private fun populateInitialValues() {
        populateWorkDateValues()
        if (mainActivity.mainViewModel.getWorkOrderHistory() != null) {
            populateFromHistory()
        }
        if (mainActivity.mainViewModel.getTempWorkOrderHistoryInfo() != null) {
            populateFromTempValues()
        }
        populateWorkOrderListForAutoComplete()
        populateWorkPerformedListForAutoComplete()
    }

    private fun populateWorkOrderHistoryWorkPerformed() {
        mainActivity.workOrderViewModel.getWorkPerformedCombinedByWorkOrderHistory(
            curHistory.history.woHistoryId
        ).observe(viewLifecycleOwner) { list ->
            val workPerFormedActualList =
                ArrayList<WorkPerformedInSequence>()
            var seq = 0
            list.listIterator().forEach {
                seq++
                workPerFormedActualList.add(
                    WorkPerformedInSequence(
                        it.workOrderHistoryWorkPerformed.workOrderHistoryWorkPerformedId,
                        it.workPerformed.workPerformedId,
                        it.workPerformed.wpDescription,
                        seq
                    )
                )
            }
            populateWorkPerformedRecycler(workPerFormedActualList)
            determineSequence(list)
        }
    }

    private fun determineSequence(list: List<WorkOrderHistoryWorkPerformedCombined>) {
        if (list.isNotEmpty()) {
            workPerformedSequence = list.last().workOrderHistoryWorkPerformed.wowpSequence
        }
    }

    private fun populateWorkPerformedRecycler(
        workPerFormedActualList: ArrayList<WorkPerformedInSequence>
    ) {
        val workPerformedAdapter =
            WorKOrderHistoryWorkPerformedAdapter(
                mainActivity,
                mView,
            )
        binding.rvWorkPerformed.apply {
            layoutManager = LinearLayoutManager(
                mView.context
            )
            adapter = workPerformedAdapter
        }
        workPerformedAdapter.differ.submitList(
            workPerFormedActualList
        )
    }

    private fun populateWorkPerformedListForAutoComplete() {
        mainActivity.workOrderViewModel.getWorkPerformedAll()
            .observe(viewLifecycleOwner) { list ->
                workPerformedListForAutoComplete.clear()
                val workPerformedDescriptions = ArrayList<String>()
                list.listIterator().forEach {
                    workPerformedListForAutoComplete.add(it)
                    workPerformedDescriptions.add(it.wpDescription)
                }
                binding.apply {
                    val wpAdapter = ArrayAdapter(
                        mView.context,
                        R.layout.spinner_item_normal,
                        workPerformedDescriptions
                    )
                    acWorkPerformed.setAdapter(wpAdapter)
                }
            }
    }

    private fun populateFromTempValues() {
        val tempWorkOrderInfo =
            mainActivity.mainViewModel.getTempWorkOrderHistoryInfo()!!
        mainActivity.workOrderViewModel.getWorkOrder(
            tempWorkOrderInfo.woHistoryWorkOrderNumber
        ).observe(viewLifecycleOwner) { workOrder ->
            curWorkOrder = workOrder
        }
        binding.apply {
            acWorkOrder.setText(
                tempWorkOrderInfo.woHistoryWorkOrderNumber
            )
            etRegHours.setText(
                nf.getNumberFromDouble(
                    tempWorkOrderInfo.woHistoryRegHours
                )
            )
            etOtHours.setText(
                nf.getNumberFromDouble(
                    tempWorkOrderInfo.woHistoryOtHours
                )
            )
            etDblOtHours.setText(
                nf.getNumberFromDouble(
                    tempWorkOrderInfo.woHistoryDblOtHours
                )
            )
            etNote.setText(
                tempWorkOrderInfo.woHistoryNote
            )
        }
    }

    private fun populateFromHistory() {
        val historyId = mainActivity.mainViewModel.getWorkOrderHistory()!!.woHistoryId
        mainActivity.workOrderViewModel.getWorkOrderHistory(historyId)
            .observe(viewLifecycleOwner) { history ->
                curHistory = history
                binding.apply {
                    acWorkOrder.setText(history.workOrder.woNumber)
                    mainActivity.workOrderViewModel.getWorkOrder(
                        history.workOrder.woNumber
                    ).observe(viewLifecycleOwner) { workOrder ->
                        curWorkOrder = workOrder
                        populateWorkOrderHistoryWorkPerformed()
                    }
                    etRegHours.setText(
                        nf.getNumberFromDouble(history.history.woHistoryRegHours)
                    )
                    etOtHours.setText(
                        nf.getNumberFromDouble(history.history.woHistoryOtHours)
                    )
                    etDblOtHours.setText(
                        nf.getNumberFromDouble(history.history.woHistoryDblOtHours)
                    )
                    etNote.setText(history.history.woHistoryNote)
                    btnEditWorkOrder.visibility = View.VISIBLE
                }
                if (mainActivity.mainViewModel.getWorkOrderNumber() != null) {
                    mainActivity.workOrderViewModel.getWorkOrder(
                        curHistory.history.woHistoryWorkOrderId
                    ).observe(
                        viewLifecycleOwner
                    ) { workOrder ->
                        curWorkOrder = workOrder
                        displayWorkOrderInfo(workOrder)
                    }
                }
            }
    }

    private fun populateWorkDateValues() {
        if (mainActivity.mainViewModel.getWorkDateObject() != null) {
            workDateObject = mainActivity.mainViewModel.getWorkDateObject()!!
            binding.apply {
                lblDate.text = df.getDisplayDate(workDateObject.wdDate)
                if (mainActivity.mainViewModel.getEmployer() != null) {
                    curEmployer = mainActivity.mainViewModel.getEmployer()!!
                    tvEmployers.text = curEmployer.employerName
                }
            }
        }
    }

    private fun setOnWorkOrderSelected(workOrderId: String) {
        mainActivity.mainViewModel.setWorkOrderNumber(workOrderId)
        mainActivity.workOrderViewModel.getWorkOrder(workOrderId).observe(
            viewLifecycleOwner
        ) { workOrder ->
            mainActivity.mainViewModel.setWorkOrder(workOrder)
        }
        setTempWorkOrderInfo()
    }

    private fun populateWorkOrderListForAutoComplete() {
        workOrderList = getWorkOrderListForAutoComplete()
        binding.apply {
            val woAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_normal, workOrderList
            )
            acWorkOrder.setAdapter(woAdapter)
        }
    }

    private fun getWorkOrderListForAutoComplete(): ArrayList<String> {
        val newList = ArrayList<String>()
        mainActivity.workOrderViewModel.getWorkOrdersByEmployerId(
            workDateObject.wdEmployerId
        ).observe(viewLifecycleOwner) { list ->
            list.listIterator().forEach {
                newList.add(it.woNumber)
            }
        }
        return newList
    }

    private fun setClickActions() {
        binding.apply {
            fabDone.setOnClickListener {
                validateWorkOrderNumber()
            }
            btnEditWorkOrder.setOnClickListener {
                setOnWorkOrderSelected(acWorkOrder.text.toString())
                mainActivity.mainViewModel.setCallingFragment(TAG)
                gotoWorkOrderUpdateFragment()
            }
            onSelectWorkOrder()
            acWorkPerformed.setOnItemClickListener { _, _, _, _ ->
                setCurWorkPerformed()
            }
            btnAddWorkPerformed.setOnClickListener {
                saveWorkPerformedIfValidAndAddToWorkOrder()
            }
        }
    }

    private fun saveWorkPerformedIfValidAndAddToWorkOrder() {
        binding.apply {
            if (acWorkPerformed.text.isNullOrBlank()) {
                Toast.makeText(
                    mView.context,
                    "Please add a valid description of work performed to add it.",
                    Toast.LENGTH_LONG
                ).show()
            } else if (setCurWorkPerformed()) {
                addWorkPerformedToWorkOrder(curWorkPerformed!!)
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    val workPerformed = insertNewWorkPerformed()
                    delay(WAIT_250)
                    addWorkPerformedToWorkOrder(workPerformed)
                }
            }
        }
    }

    private fun addWorkPerformedToWorkOrder(workPerformed: WorkPerformed) {
        workPerformedSequence++
        mainActivity.workOrderViewModel.insertWorkOrderHistoryWorkPerformed(
            WorkOrderHistoryWorkPerformed(
                nf.generateRandomIdAsLong(),
                curHistory.history.woHistoryId,
                workPerformed.workPerformedId,
                workPerformedSequence,
                false,
                df.getCurrentTimeAsString()
            )
        )
        curWorkPerformed = null
        binding.acWorkPerformed.text.clear()
    }

    private fun insertNewWorkPerformed(): WorkPerformed {
        val workPerformed =
            WorkPerformed(
                nf.generateRandomIdAsLong(),
                binding.acWorkPerformed.text.toString(),
                false,
                df.getCurrentTimeAsString()
            )
        mainActivity.workOrderViewModel.insertWorkPerformed(
            workPerformed
        )
        return workPerformed
    }

    private fun setCurWorkPerformed(): Boolean {
        binding.apply {
            for (workPerformed in workPerformedListForAutoComplete) {
                if (acWorkPerformed.text.toString() ==
                    workPerformed.wpDescription
                ) {
                    curWorkPerformed = workPerformed
                    return true
                }
            }
        }
        return false
    }

    private fun saveCurrentHistoryIfValidAndGotoUpdateFragment() {
        binding.apply {
            if (acWorkOrder.text.isNullOrBlank()) {
                Toast.makeText(
                    mView.context,
                    "Please enter a valid work order number before adding work performed",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                prepareToUpdate()
            }
        }
    }

    private fun validateWorkOrderNumber() {
        if (checkIfWorOrderExists()) {
            prepareToUpdate()
        } else {
            AlertDialog.Builder(mView.context)
                .setTitle(
                    "Create Work Order: " +
                            "${binding.acWorkOrder.text}?"
                )
                .setMessage(
                    "This Work Order does not exist." +
                            "Would you like to create a new one?"
                )
                .setPositiveButton("Yes") { _, _ ->
                    gotoWorkOrderAddFragment()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun checkIfWorOrderExists(): Boolean {
        for (workOrder in workOrderList) {
            if (binding.acWorkOrder.text.toString() == workOrder) {
                return true
            }
        }
        return false
    }

    private fun setTempWorkOrderInfo() {
        binding.apply {
            mainActivity.mainViewModel.setTempWorkOrderHistoryInfo(
                TempWorkOrderHistoryInfo(
                    if (acWorkOrder.text.isNullOrBlank())
                        "0" else acWorkOrder.text.toString(),
                    lblDate.text.toString(),
                    if (etRegHours.text.isNullOrBlank())
                        0.0 else etRegHours.text.toString().toDouble(),
                    if (etOtHours.text.isNullOrBlank())
                        0.0 else etOtHours.text.toString().toDouble(),
                    if (etDblOtHours.text.isNullOrBlank())
                        0.0 else etDblOtHours.text.toString().toDouble(),
                    if (etNote.text.isNullOrBlank())
                        null else etNote.text.toString()
                )
            )
        }
    }

    private fun prepareToUpdate() {
        val answer = validateEntry()
        if (answer == ANSWER_OK) {
            updateHistory()
        } else {
            Toast.makeText(
                mView.context,
                answer, Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateHistory() {
        mainActivity.mainViewModel.setTempWorkOrderHistoryInfo(null)
        val history = getCurHistory()
        mainActivity.workOrderViewModel.updateWorkOrderHistory(
            history.woHistoryId,
            curWorkOrder.workOrderId,
            history.woHistoryWorkDateId,
            history.woHistoryRegHours,
            history.woHistoryOtHours,
            history.woHistoryDblOtHours,
            history.woHistoryNote,
            false,
            history.woHistoryUpdateTime
        )
        gotoCallingFragment()
    }

    private fun getCurHistory(): WorkOrderHistory {
        binding.apply {
            return WorkOrderHistory(
                curHistory.history.woHistoryId,
                curWorkOrder.workOrderId,
                workDateObject.workDateId,
                if (etRegHours.text.isNullOrBlank())
                    0.0 else etRegHours.text.toString().toDouble(),
                if (etOtHours.text.isNullOrBlank())
                    0.0 else etOtHours.text.toString().toDouble(),
                if (etDblOtHours.text.isNullOrBlank())
                    0.0 else etDblOtHours.text.toString().toDouble(),
                if (etNote.text.isNullOrBlank())
                    null else etNote.text.toString(),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun validateEntry(): String {
        binding.apply {
            etRegHours.setText(
                if (etRegHours.text.isNullOrBlank()) {
                    "0.0"
                } else {
                    etRegHours.text.toString().toDouble().toString()
                }
            )
            etOtHours.setText(
                if (etOtHours.text.isNullOrBlank()) {
                    "0.0"
                } else {
                    etOtHours.text.toString().toDouble().toString()
                }
            )
            etDblOtHours.setText(
                if (etDblOtHours.text.isNullOrBlank()) {
                    "0.0"
                } else {
                    etDblOtHours.text.toString().toDouble().toString()
                }
            )
            if (etRegHours.text.toString().toDouble() == 0.0
                && etOtHours.text.toString().toDouble() == 0.0
                && etDblOtHours.text.toString().toDouble() == 0.0
            ) {
                return "There was no time entered. Please enter some time."
            }
        }
        return ANSWER_OK
    }

    private fun gotoCallingFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections
                .actionWorkOrderHistoryUpdateFragmentToWorkDateUpdateFragment()
        )
    }

    private fun gotoWorkOrderUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections
                .actionWorkOrderHistoryUpdateFragmentToWorkOrderUpdateFragment()
        )
    }

    private fun gotoWorkOrderAddFragment() {
        setTempWorkOrderInfo()
        mainActivity.mainViewModel.setCallingFragment(TAG)
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections
                .actionWorkOrderHistoryUpdateFragmentToWorkOrderAddFragment()
        )
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}