package ms.mattschlenkrich.paydaycalculator.ui.workOrder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.adapter.workorders.WorkOrderHistoryAdapter
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.FRAG_WORK_ODER_HISTORY_UPDATE
import ms.mattschlenkrich.paydaycalculator.common.FRAG_WORK_ORDERS
import ms.mattschlenkrich.paydaycalculator.common.FRAG_WORK_ORDER_HISTORY_ADD
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkOrderAddBinding
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.model.workOrder.WorkOrder
import ms.mattschlenkrich.paydaycalculator.model.workOrder.WorkOrderHistoryFull
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class WorkOrderUpdateFragment : Fragment(R.layout.fragment_work_order_add) {

    private var _binding: FragmentWorkOrderAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity

    private val df = DateFunctions()

    private val nf = NumberFunctions()
    private val workOrderList = ArrayList<WorkOrder>()
    private lateinit var curEmployer: Employers
    private lateinit var curWorkOrder: WorkOrder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkOrderAddBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.update_work_order)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDefaultValues()
        getWorkOrderList()
        setClickActions()
        onSelectEmployer()
    }

    private fun populateHistory(workOrderId: Long) {
        mainActivity.workOrderViewModel.getWorkOrderHistoriesById(
            workOrderId
        ).observe(viewLifecycleOwner) { historyList ->
            calculateTotals(historyList)
            val workOrderHistoryAdapter =
                WorkOrderHistoryAdapter(
                    mainActivity,
                    mView,
                    historyList
                )
            binding.rvHistory.apply {
                layoutManager = LinearLayoutManager(mView.context)
                adapter = workOrderHistoryAdapter
            }
        }
    }

    private fun calculateTotals(historyList: List<WorkOrderHistoryFull>) {
        var regHours = 0.0
        var otHours = 0.0
        var dblOtHours = 0.0
        historyList.listIterator().forEach {
            regHours += it.history.woHistoryRegHours
            otHours += it.history.woHistoryOtHours
            dblOtHours += it.history.woHistoryDblOtHours
        }
        var display = ""
        if (regHours != 0.0) {
            display = "Reg: " +
                    nf.getNumberFromDouble(
                        regHours
                    )
        }
        if (otHours != 0.0) {
            if (display.isNotBlank()) {
                display += " | "
            }
            display += "Ot: " +
                    nf.getNumberFromDouble(
                        otHours
                    )
        }
        if (dblOtHours != 0.0) {
            if (display.isNotBlank()) {
                display += " | "
            }
            display += "Ot: " +
                    nf.getNumberFromDouble(
                        dblOtHours
                    )
        }
        binding.tvWorkOrderSummary.text = display
    }

    private fun setDefaultValues() {
        if (mainActivity.mainViewModel.getEmployer() != null) {
            curEmployer = mainActivity.mainViewModel.getEmployer()!!
            binding.apply {
                spEmployers.visibility = View.INVISIBLE
                tvEmployer.visibility = View.VISIBLE
                tvEmployer.text = curEmployer.employerName
            }
            getWorkOrderList()
        } else {
            binding.apply {
                spEmployers.visibility = View.VISIBLE
                tvEmployer.visibility = View.INVISIBLE
            }
            populateEmployers()
        }
        mainActivity.workOrderViewModel.getWorkOrder(
            mainActivity.mainViewModel.getWorkOrderNumber()!!
        ).observe(viewLifecycleOwner) { tempWorkOrder ->
            curWorkOrder = tempWorkOrder
            setValuesFromHistory(curWorkOrder)
        }
    }

    private fun setValuesFromHistory(workOrder: WorkOrder) {

        binding.apply {
            etWorkOrderNumber.setText(workOrder.woNumber)
            etAddress.setText(workOrder.woAddress)
            etDescription.setText(workOrder.woDescription)
        }
        populateHistory(workOrder.workOrderId)
    }

    private fun populateEmployers() {
        mainActivity.employerViewModel.getEmployers().observe(
            viewLifecycleOwner
        ) { list ->
            binding.apply {
                val employerAdapter = ArrayAdapter<Any>(
                    mView.context,
                    R.layout.spinner_item_bold
                )
                list.listIterator().forEach {
                    employerAdapter.add(it.employerName)
                }
                spEmployers.adapter = employerAdapter
            }
        }
    }

    private fun getWorkOrderList() {
        workOrderList.clear()
        mainActivity.workOrderViewModel.getWorkOrdersByEmployerId(
            curEmployer.employerId
        ).observe(
            viewLifecycleOwner
        ) { list ->
            list.listIterator().forEach {
                workOrderList.add(it)
            }
        }
    }

    private fun setClickActions() {
        binding.apply {
            fabDone.setOnClickListener {
                prepareToUpdate()
            }
        }
    }

    private fun prepareToUpdate() {
        val answer = validateWorkOrder()
        if (answer == ANSWER_OK) {
            updateWorkOrder()
        } else {
            Toast.makeText(
                mView.context,
                answer, Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateWorkOrder() {
        val workOrder = getCurrentWorkOrder()
        mainActivity.workOrderViewModel.updateWorkOrder(
            workOrder.workOrderId,
            workOrder.woNumber,
            workOrder.woEmployerId,
            workOrder.woAddress,
            workOrder.woDescription,
            false,
            df.getCurrentTimeAsString()
        )
        gotoCallingFragment()
    }

    private fun gotoCallingFragment() {
        when (mainActivity.mainViewModel.getCallingFragment()) {

            FRAG_WORK_ORDER_HISTORY_ADD -> {
                gotoWorkOrderHistoryAddFragment()
            }

            FRAG_WORK_ODER_HISTORY_UPDATE -> {
                gotoWorkOrderHistoryUpdateFragment()
            }

            FRAG_WORK_ORDERS -> {
                gotoWorkOrdersFragment()
            }
        }
    }

    private fun gotoWorkOrdersFragment() {
        mView.findNavController().navigate(
            WorkOrderUpdateFragmentDirections
                .actionWorkOrderUpdateFragmentToWorkOrdersFragment()
        )
    }

    private fun gotoWorkOrderHistoryUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderUpdateFragmentDirections
                .actionWorkOrderUpdateFragmentToWorkOrderHistoryUpdateFragment()
        )
    }

    private fun gotoWorkOrderHistoryAddFragment() {
        mView.findNavController().navigate(
            WorkOrderUpdateFragmentDirections
                .actionWorkOrderUpdateFragmentToWorkOrderHistoryAddFragment()
        )
    }

    private fun getCurrentWorkOrder(): WorkOrder {
        binding.apply {
            return WorkOrder(
                curWorkOrder.workOrderId,
                etWorkOrderNumber.text.toString(),
                curEmployer.employerId,
                etAddress.text.toString(),
                etDescription.text.toString(),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun onSelectEmployer() {
        binding.apply {
            spEmployers.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        CoroutineScope(Dispatchers.IO).launch {
                            curEmployer = mainActivity.employerViewModel.findEmployer(
                                spEmployers.selectedItem.toString()
                            )
                        }
                        getWorkOrderList()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        //not needed
                    }
                }
        }
    }

    private fun validateWorkOrder(): String {
        binding.apply {

            if (etAddress.text.isEmpty()) {
                return getString(R.string.please_enter_an_address)
            }
            if (etDescription.text.isEmpty()) {
                return getString(R.string.please_enter_a_description)
            }
        }
        return ANSWER_OK
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}