package ms.mattschlenkrich.paycalculator.ui.workorder.workorder

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkOrderViewBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.workorder.workorder.adapter.WorkOrderLookupAdapter

//private const val TAG = FRAG_WORK_ORDER_LOOKUP

class WorkOrderLookupFragment : Fragment(R.layout.fragment_work_order_view) {

    private var _binding: FragmentWorkOrderViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private var curEmployer: Employers? = null
    private var workOrderAdapter: WorkOrderLookupAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkOrderViewBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel
        mainActivity.title = getString(R.string.choose_a_work_order)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateInitialView()
        setClickActions()
    }

    private fun populateInitialView() {
        binding.apply {
            tvEmployer.visibility = View.VISIBLE
            spEmployers.visibility = View.INVISIBLE
            fabNew.visibility = View.GONE
            if (mainActivity.mainViewModel.getEmployer() != null) {
                populateEmployer()
            }
        }
    }

    private fun populateEmployer() {
        binding.apply {
            curEmployer = mainViewModel.getEmployer()
            tvEmployer.text = curEmployer?.employerName
            populateWorkOrders(curEmployer!!.employerId)
        }
    }

    private fun populateWorkOrders(employerId: Long) {
        workOrderAdapter = WorkOrderLookupAdapter(
            mainActivity, mView, this@WorkOrderLookupFragment
        )
        workOrderViewModel.getWorkOrdersByEmployerId(employerId).observe(
            viewLifecycleOwner
        ) { list ->
            workOrderAdapter!!.differ.submitList(list)
        }
        binding.rvWorkOrders.apply {
            layoutManager = LinearLayoutManager(mView.context)
            adapter = workOrderAdapter!!
        }
    }

    private fun setClickActions() {
        binding.apply {
            etWorkOrder.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
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
        if (workOrderAdapter != null && curEmployer != null) {
            val searchQuery = "%$query%"
            workOrderViewModel.searchWorkOrders(curEmployer!!.employerId, searchQuery).observe(
                viewLifecycleOwner
            ) { list ->
                workOrderAdapter!!.differ.submitList(list)
            }
        }
    }

    fun gotoWorkOrderHistoryAddFragment() {
        mView.findNavController().navigate(
            WorkOrderLookupFragmentDirections.actionWorkOrderLookupFragmentToWorkOrderHistoryAddFragment()
        )
    }

    fun gotoWorkOrderHistoryUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderLookupFragmentDirections.actionWorkOrderLookupFragmentToWorkOrderHistoryUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}