package ms.mattschlenkrich.paycalculator.ui.workorder.workorder

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrder
import ms.mattschlenkrich.paycalculator.database.viewModel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkOrderBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity

class WorkOrderAddFragment : Fragment(R.layout.fragment_work_order) {

    private var _binding: FragmentWorkOrderBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private lateinit var employerViewModel: EmployerViewModel
    private lateinit var workOrderList: List<WorkOrder>
    private lateinit var curEmployer: Employers
    private lateinit var curWorkOrder: WorkOrder
    private val df = DateFunctions()
    private val nf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkOrderBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel
        employerViewModel = mainActivity.employerViewModel
        mainActivity.title = getString(R.string.add_new_work_order)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateInitialValues()
        setClickActions()
    }

    private fun populateInitialValues() {
        hideJobSpecsAndHistory()
        binding.apply {
            if (mainViewModel.getEmployer() != null) {
                curEmployer = mainViewModel.getEmployer()!!
                spEmployers.visibility = View.INVISIBLE
                tvEmployer.visibility = View.VISIBLE
                tvEmployer.text = curEmployer.employerName
                populateWorkOrderListForValidation()
            } else {
                spEmployers.visibility = View.VISIBLE
                tvEmployer.visibility = View.INVISIBLE
                populateEmployers()
            }
            if (mainViewModel.getTempWorkOrderHistoryInfo() != null) {
                populateValuesFromHistory()
//                mainActivity.mainViewModel.setTempWorkOrderHistoryInfo(null)
            }
            crdHistory.visibility = View.INVISIBLE
        }
    }

    private fun hideJobSpecsAndHistory() {
        binding.apply {
            crdJobSpecs.visibility = View.GONE
            crdHistory.visibility = View.GONE
        }
    }

    private fun populateWorkOrderListForValidation() {
        workOrderViewModel.getWorkOrdersByEmployerId(curEmployer.employerId).observe(
            viewLifecycleOwner
        ) { list ->
            workOrderList = list
        }
    }

    private fun populateEmployers() {
        employerViewModel.getEmployers().observe(
            viewLifecycleOwner
        ) { list ->
            binding.apply {
                val employerAdapter = ArrayAdapter<Any>(mView.context, R.layout.spinner_item_bold)
                list.listIterator().forEach { employerAdapter.add(it.employerName) }
                spEmployers.adapter = employerAdapter
            }
        }
    }

    private fun populateValuesFromHistory() {
        val tempWorkOrder = mainActivity.mainViewModel.getTempWorkOrderHistoryInfo()!!
        binding.etWorkOrderNumber.setText(tempWorkOrder.woHistoryWorkOrderNumber)
    }

    private fun setClickActions() {
        binding.fabDone.setOnClickListener {
            saveWorkOrderAndAddJobSpecIfValid()
            onSelectEmployer()
        }
    }

    private fun saveWorkOrderAndAddJobSpecIfValid() {
        val answer = validateWorkOrder()
        if (answer == ANSWER_OK) {
            saveWorkOrderAndAChooseNextSteps()
        } else {
            displayMessage(getString(R.string.error_) + answer)
        }
    }

    private fun saveWorkOrderAndAChooseNextSteps() {
        curWorkOrder = getCurrentWorkOrder()
        workOrderViewModel.insertWorkOrder(curWorkOrder)
        mainViewModel.apply {
            setWorkOrder(curWorkOrder)
            setWorkOrderNumber(curWorkOrder.woNumber)
        }
        chooseToGotoUpdate()
    }

    private fun chooseToGotoUpdate() {
        AlertDialog.Builder(mView.context).setTitle(getString(R.string.choose_the_next_step))
            .setMessage(getString(R.string.would_you_like_to_update_job_specs_for_this_work_order))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> gotoWorkOrderUpdate() }
            .setNegativeButton(getString(R.string.no)) { _, _ -> gotoCallingFragment() }
            .setNeutralButton(getString(R.string.cancel), null).show()
    }

    private fun onSelectEmployer() {
        binding.apply {
            spEmployers.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    CoroutineScope(Dispatchers.IO).launch {
                        curEmployer = mainActivity.employerViewModel.findEmployer(
                            spEmployers.selectedItem.toString()
                        )
                    }
                    populateWorkOrderListForValidation()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    //not needed
                }
            }
        }
    }

    private fun validateWorkOrder(): String {
        binding.apply {
            if (etWorkOrderNumber.text.isEmpty()) {
                return getString(R.string.please_enter_a_work_order_number)
            }
            for (workOrder in workOrderList) {
                if (workOrder.woNumber == etWorkOrderNumber.text.toString()) {
                    return getString(R.string.this_work_order_has_been_used)
                }
            }
            if (etAddress.text.isEmpty()) {
                return getString(R.string.please_enter_an_address)
            }
            if (etDescription.text.isEmpty()) {
                return getString(R.string.please_enter_a_description)
            }
        }
        return ANSWER_OK
    }

    private fun gotoWorkOrderUpdate() {
        displayMessage(getString(R.string.work_order_has_been_added_automatically_before_adding_work_specs))
        gotoWorkOrderUpdateFragment()
    }

    private fun displayMessage(message: String) {
        Toast.makeText(mView.context, message, Toast.LENGTH_LONG).show()
    }

    private fun gotoCallingFragment() {
        gotoTimeSheetAddWorkOrderFragment()
    }

    private fun gotoWorkOrderUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderAddFragmentDirections.actionWorkOrderAddFragmentToWorkOrderUpdateFragment()
        )
    }

    private fun gotoTimeSheetAddWorkOrderFragment() {
        mView.findNavController().navigate(
            WorkOrderAddFragmentDirections.actionWorkOrderAddFragmentToWorkOrderHistoryAddFragment()
        )
    }

    private fun getCurrentWorkOrder(): WorkOrder {
        binding.apply {
            return WorkOrder(
                nf.generateRandomIdAsLong(),
                etWorkOrderNumber.text.toString(),
                curEmployer.employerId,
                etAddress.text.toString().trim(),
                etDescription.text.toString().trim(),
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