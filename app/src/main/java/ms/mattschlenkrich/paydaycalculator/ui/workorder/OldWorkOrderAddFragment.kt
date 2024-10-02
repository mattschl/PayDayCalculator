package ms.mattschlenkrich.paydaycalculator.ui.workorder

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
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkOrder
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkOrderAddBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class OldWorkOrderAddFragment : Fragment(R.layout.fragment_work_order_add) {

    private var _binding: FragmentWorkOrderAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity

    private val df = DateFunctions()

    private val nf = NumberFunctions()
    private val workOrderList = ArrayList<WorkOrder>()
    private lateinit var curEmployer: Employers

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkOrderAddBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.add_new_work_order)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDefaultValues()
        getWorkOrderList()
        setClickActions()
        onSelectEmployer()
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
        if (mainActivity.mainViewModel.getTempWorkOrderHistoryInfo() != null) {
            setValuesFromHistory()
        }
    }

    private fun setValuesFromHistory() {
        val tempWorkOrder = mainActivity.mainViewModel.getTempWorkOrderHistoryInfo()!!
        binding.apply {
            etWorkOrderNumber.setText(tempWorkOrder.woHistoryWorkOrderNumber)
        }
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
                prepareToSave()
            }
        }
    }

    private fun prepareToSave() {
        val answer = validateWorkOrder()
        if (answer == ANSWER_OK) {
            saveWorkOrder()
        } else {
            Toast.makeText(
                mView.context,
                answer, Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun saveWorkOrder() {
        val newWorkOrder = getCurrentWorkOrder()
        mainActivity.workOrderViewModel.insertWorkOrder(newWorkOrder)
        mainActivity.mainViewModel.setWorkOrder(newWorkOrder)
        mainActivity.mainViewModel.setWorkOrderNumber(
            newWorkOrder.woNumber
        )
        gotoCallingFragment()
    }

    private fun gotoCallingFragment() {
        gotoTimeSheetAddWorkOrderFragment()
    }

    private fun gotoTimeSheetAddWorkOrderFragment() {
        mView.findNavController().navigate(
            WorkOrderAddFragmentDirections
                .actionWorkOrderAddFragmentToWorkOrderHistoryAddFragment()
        )
    }

    private fun getCurrentWorkOrder(): WorkOrder {
        binding.apply {
            return WorkOrder(
                nf.generateRandomIdAsLong(),
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
            if (etWorkOrderNumber.text.isEmpty()) {
                return getString(R.string.please_enter_a_work_order_number)
            }
            for (workOrder in workOrderList) {
                if (workOrder.woNumber ==
                    etWorkOrderNumber.text.toString()
                ) {
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}