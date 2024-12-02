package ms.mattschlenkrich.paydaycalculator.ui.workorder

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.FRAG_WORK_ORDERS
import ms.mattschlenkrich.paydaycalculator.common.WAIT_250
import ms.mattschlenkrich.paydaycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkOrderViewBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import ms.mattschlenkrich.paydaycalculator.ui.workorder.adapter.WorkOrdersAdapter

private const val TAG = FRAG_WORK_ORDERS

class WorkOrderViewFragment :
    Fragment(R.layout.fragment_work_order_view) {

    private var _binding: FragmentWorkOrderViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private var curEmployer: Employers? = null
    private var workOrderAdapter: WorkOrdersAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkOrderViewBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity.title = getString(R.string.work_orders)
        populateEmployers()
        setClickActions()
    }

    private fun populateEmployers() {
        val employerAdapter = ArrayAdapter<String>(
            mView.context,
            R.layout.spinner_item_bold
        )
        mainActivity.employerViewModel.getEmployers().observe(
            viewLifecycleOwner
        ) { employers ->
            employerAdapter.clear()
            employerAdapter.notifyDataSetChanged()
            employers.listIterator().forEach {
                employerAdapter.add(it.employerName)
            }
            curEmployer = employers.first()
            employerAdapter.add(getString(R.string.add_new_employer))
        }
        binding.spEmployers.adapter = employerAdapter
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
                        if (spEmployers.selectedItem.toString() !=
                            getString(R.string.add_new_employer)
                        ) {
                            CoroutineScope(Dispatchers.IO).launch {
                                curEmployer = mainActivity.employerViewModel.findEmployer(
                                    spEmployers.selectedItem.toString()
                                )
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(WAIT_250)
                                mainActivity.mainViewModel.setEmployer(curEmployer)
                                populateWorkOrders(curEmployer!!.employerId)
                            }
                        } else {
                            gotoEmployerAdd()
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
//                        fillEmployers()
                    }
                }
        }
    }

    private fun populateWorkOrders(employerId: Long) {
        workOrderAdapter = WorkOrdersAdapter(
            mainActivity, mView, TAG
        )
        mainActivity.workOrderViewModel.getWorkOrdersByEmployerId(
            employerId
        ).observe(viewLifecycleOwner) { list ->
            workOrderAdapter!!.differ.submitList(list)
        }
        binding.rvWorkOrders.apply {
            layoutManager =
                LinearLayoutManager(mView.context)
            adapter = workOrderAdapter!!
        }
    }

    private fun gotoEmployerAdd() {
        mainActivity.mainViewModel.setCallingFragment(TAG)
        mainActivity.mainViewModel.setEmployer(null)
        mView.findNavController().navigate(
            WorkOrderViewFragmentDirections
                .actionWorkOrderViewFragmentToEmployerAddFragment()
        )
    }

    private fun setClickActions() {
        onSelectEmployer()
        binding.apply {
            fabNew.setOnClickListener {
                addNewWorkOrder()
            }
            etWorkOrder.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
//                    null
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                    null
                }

                override fun afterTextChanged(s: Editable?) {
                    searchWorkOrders(etWorkOrder.text.toString())
                }
            })
            btnCancel.setOnClickListener {
                resetSearch()
            }
        }
    }

    private fun resetSearch() {
        binding.etWorkOrder.setText(getString(R.string.blank))
        populateWorkOrders(curEmployer!!.employerId)
    }

    private fun searchWorkOrders(query: String) {
        if (workOrderAdapter != null &&
            curEmployer != null
        ) {
            val searchQuery = "%$query%"
            mainActivity.workOrderViewModel.searchWorkOrders(
                curEmployer!!.employerId, searchQuery
            ).observe(viewLifecycleOwner) { list ->
                workOrderAdapter!!.differ.submitList(list)
            }
        }
    }

    private fun addNewWorkOrder() {
        mainActivity.mainViewModel.setEmployer(curEmployer)
        mView.findNavController().navigate(
            WorkOrderViewFragmentDirections
                .actionWorkOrderViewFragmentToWorkOrderAddFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}