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
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.FRAG_WORK_ORDER_HISTORY_ADD
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.TempWorkOrderHistoryInfo
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkOrder
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkOrderHistory
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkOrderHistoryBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

private const val TAG = FRAG_WORK_ORDER_HISTORY_ADD

class WorkOrderHistoryAddFragment :
    Fragment(R.layout.fragment_work_order_history) {

    private var _binding: FragmentWorkOrderHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private val workOrderList =
        ArrayList<WorkOrder>()
    private val workOrderListForAutocomplete =
        ArrayList<String>()
    private var workDateObject: WorkDates? = null
    private var curEmployer: Employers? = null
    private var curWorkOrder: WorkOrder? = null
    private lateinit var curHistory: WorkOrderHistory
    private lateinit var commonFunctions:
            WorkOrderCommonFunctions

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkOrderHistoryBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        commonFunctions =
            WorkOrderCommonFunctions(mainActivity)
        mainActivity.title = getString(R.string.add_time_to_work_order)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        hideMaterialAndWorkPerformed()
        populateWorkDate()
        if (workDateObject != null) {
            populateCurrentEmployer()
        }
        populateTempWorkOrderInfo()
        populateWorkOrderListInAutoComplete()
    }

    private fun hideMaterialAndWorkPerformed() {
        binding.apply {
            crdMaterials.visibility = View.GONE
            crdWorkPerformed.visibility = View.GONE
        }
    }

    private fun populateWorkDate() {
        workDateObject = commonFunctions.getWorkDateObject()
        if (workDateObject != null) {
            binding.lblDate.text = df.getDisplayDate(workDateObject!!.wdDate)
        }
    }

    private fun populateCurrentEmployer() {
        curEmployer = commonFunctions.getEmployer()
        if (curEmployer != null) {
            binding.tvEmployers.text =
                curEmployer!!.employerName
        }
    }

    private fun populateTempWorkOrderInfo() {
        if (mainActivity.mainViewModel.getTempWorkOrderHistoryInfo() != null) {
            val tempWorkOrderHistory =
                mainActivity.mainViewModel.getTempWorkOrderHistoryInfo()!!
            binding.apply {
                if (mainActivity.mainViewModel.getWorkOrderNumber() != null) {
                    acWorkOrder.setText(
                        mainActivity.mainViewModel.getWorkOrderNumber()!!
                    )
                } else {
                    acWorkOrder.setText(
                        tempWorkOrderHistory.woHistoryWorkOrderNumber
                    )
                }
                etRegHours.setText(
                    nf.getNumberFromDouble(
                        tempWorkOrderHistory.woHistoryRegHours
                    )
                )
                etOtHours.setText(
                    nf.getNumberFromDouble(
                        tempWorkOrderHistory.woHistoryOtHours
                    )
                )
                etDblOtHours.setText(
                    nf.getNumberFromDouble(
                        tempWorkOrderHistory.woHistoryDblOtHours
                    )
                )
                etNote.setText(
                    tempWorkOrderHistory.woHistoryNote
                )
                setCurWorkOrder()
                mainActivity.mainViewModel.setTempWorkOrderHistoryInfo(null)
            }
        }
    }

    private fun populateWorkOrderListInAutoComplete() {
        getWorkOrderLists()
        binding.apply {
            val woAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_normal, workOrderListForAutocomplete
            )
            acWorkOrder.setAdapter(woAdapter)
        }
    }

    private fun populateWorkOrderInfo() {
        binding.apply {
            if (curWorkOrder != null) {
                val display =
                    curWorkOrder!!.woAddress + " - " +
                            curWorkOrder!!.woNumber
                tvDescription.text = display
                tvDescription.visibility = View.VISIBLE
            }
        }
    }

    private fun setClickActions() {
        binding.apply {
            fabDone.setOnClickListener {
                validateWorkOrderNumberAndSaveHistoryIfValid()
            }
            btnWorkOrder.setOnClickListener {
                if (btnWorkOrder.text.toString() == getString(R.string.update)) {
                    gotoWorkOrderUpdateFragment()
                } else {
                    gotoWorkOrderAddFragment()
                }
            }
            acWorkOrder.setOnItemClickListener { _, _, _, _ ->
                setCurWorkOrder()
            }
            acWorkOrder.setOnKeyListener { _, _, _ ->
                setCurWorkOrder()
                false
            }
        }
    }

    private fun validateWorkOrderNumberAndSaveHistoryIfValid() {
        if (doesWorkOrderExist()) {
            chooseToGotoUpdate()
        } else {
            AlertDialog.Builder(mView.context)
                .setTitle(
                    "Create Work Order: " +
                            "${binding.acWorkOrder.text}?"
                )
                .setMessage(
                    "This Work Order does not exist. " +
                            "It must be created before continuing. " +
                            "Would you like to create it now?"
                )
                .setPositiveButton("Yes") { _, _ ->
                    gotoWorkOrderAddFragment()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun chooseToGotoUpdate() {

        AlertDialog.Builder(mView.context)
            .setTitle(
                "Choose next steps after saving"
            )
            .setMessage(
                "Would you like to add work performed or materials to this history?"
            )
            .setPositiveButton("Yes") { _, _ ->
                saveHistoryIfValid(true)
            }
            .setNegativeButton("No") { _, _ ->
                saveHistoryIfValid(false)
            }
            .show()
    }

    private fun validateWorkOrderHistory(): String {
        binding.apply {
            if (curWorkOrder == null &&
                acWorkOrder.text.isNullOrBlank()
            ) {
                return "There is no work order selected"
            }
            convertNumbersToDoubles()
        }
        return ANSWER_OK
    }

    private fun setCurWorkOrder() {
        binding.apply {
            if (acWorkOrder.text.isNullOrBlank()) {
                Toast.makeText(
                    mView.context,
                    "Please enter a valid work order before adding work performed",
                    Toast.LENGTH_LONG
                ).show()
            }
            if (doesWorkOrderExist()) {
                populateWorkOrderInfo()
                btnWorkOrder.text = getString(R.string.edit)
            } else {
                btnWorkOrder.text = getString(R.string.create)
                tvDescription.visibility = View.INVISIBLE
            }
        }
    }

    private fun setTempWorkOrderHistory() {
        binding.apply {
            mainActivity.mainViewModel.setTempWorkOrderHistoryInfo(
                TempWorkOrderHistoryInfo(
                    if (acWorkOrder.text.isNullOrBlank())
                        "00" else acWorkOrder.text.toString(),
                    lblDate.text.toString(),
                    if (etRegHours.text.isNullOrBlank())
                        0.0 else etRegHours.text.toString().trim().toDouble(),
                    if (etOtHours.text.isNullOrBlank())
                        0.0 else etOtHours.text.toString().trim().toDouble(),
                    if (etDblOtHours.text.isNullOrBlank())
                        0.0 else etDblOtHours.text.toString().trim().toDouble(),
                    if (etNote.text.isNullOrBlank())
                        "" else etNote.text.toString(),
                    if (acWorkPerformed.text.isNullOrBlank())
                        "" else acWorkPerformed.text.toString().trim(),
                    if (etMaterialQty.text.isNullOrBlank())
                        0.0 else etMaterialQty.text.toString().trim().toDouble(),
                    if (acMaterials.text.isNullOrBlank())
                        "" else acMaterials.text.toString().trim()
                )
            )
        }
    }

    private fun getWorkOrderLists() {
        if (workDateObject != null) {
            mainActivity.workOrderViewModel.getWorkOrdersByEmployerId(
                workDateObject!!.wdEmployerId
            ).observe(viewLifecycleOwner) { list ->
                workOrderList.clear()
                workOrderListForAutocomplete.clear()
                list.listIterator().forEach {
                    workOrderList.add(it)
                    workOrderListForAutocomplete.add(it.woNumber)
                }
            }
        }
    }

    private fun getCurHistory(): WorkOrderHistory {
        binding.apply {
            setCurWorkOrder()
            val workOrderId = curWorkOrder!!.workOrderId
            return WorkOrderHistory(
                nf.generateRandomIdAsLong(),
                workOrderId,
                workDateObject!!.workDateId,
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

    private fun saveHistoryIfValid(gotoUpdate: Boolean) {
        val answer = validateWorkOrderHistory()
        if (answer == ANSWER_OK) {
            curHistory = getCurHistory()

            saveHistory(gotoUpdate)
        } else {
            Toast.makeText(
                mView.context,
                answer, Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun saveHistory(gotoUpdate: Boolean) {
        mainActivity.workOrderViewModel.insertWorkOrderHistory(
            curHistory
        )
        if (gotoUpdate) {
            mainActivity.mainViewModel.setWorkOrderHistory(
                curHistory
            )

            gotoWorkOrderHistoryUpdateFragment()
        } else {
            mainActivity.mainViewModel.setTempWorkOrderHistoryInfo(null)
            gotoCallingFragment()
        }
    }

    private fun doesWorkOrderExist(): Boolean {
        for (workOrder in workOrderList) {
            if (binding.acWorkOrder.text.toString() == workOrder.woNumber) {
                curWorkOrder = workOrder
                return true
            }
        }
        return false
    }

    private fun convertNumbersToDoubles() {
        binding.apply {
            etRegHours.setText(
                if (etRegHours.text.isNullOrBlank()) {
                    "0.0"
                } else {
                    etRegHours.text.toString().trim().toDouble().toString()
                }
            )
            etOtHours.setText(
                if (etOtHours.text.isNullOrBlank()) {
                    "0.0"
                } else {
                    etOtHours.text.toString().trim().toDouble().toString()
                }
            )
            etDblOtHours.setText(
                if (etDblOtHours.text.isNullOrBlank()) {
                    "0.0"
                } else {
                    etDblOtHours.text.toString().trim().toDouble().toString()
                }
            )
        }
    }

    private fun gotoWorkOrderAddFragment() {
        setTempWorkOrderHistory()
        mainActivity.mainViewModel.setCallingFragment(TAG)
        mView.findNavController().navigate(
            WorkOrderHistoryAddFragmentDirections
                .actionWorkOrderHistoryAddFragmentToWorkOrderAddFragment()
        )
    }

    private fun gotoCallingFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryAddFragmentDirections
                .actionWorkOrderHistoryAddFragmentToWorkDateUpdateFragment()
        )
    }

    private fun gotoWorkOrderHistoryUpdateFragment() {
        setTempWorkOrderHistory()
        mView.findNavController().navigate(
            WorkOrderHistoryAddFragmentDirections
                .actionWorkOrderHistoryAddFragmentToWorkOrderHistoryUpdateFragment()
        )
    }

    private fun gotoWorkOrderUpdateFragment() {
        mainActivity.mainViewModel.setWorkOrderNumber(
            binding.acWorkOrder.text.toString()
        )
        setTempWorkOrderHistory()
        mainActivity.mainViewModel.setCallingFragment(TAG)
        mView.findNavController().navigate(
            WorkOrderHistoryAddFragmentDirections
                .actionWorkOrderHistoryAddFragmentToWorkOrderUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}