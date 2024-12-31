package ms.mattschlenkrich.paydaycalculator.ui.workorder.workorder

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkOrderViewBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import ms.mattschlenkrich.paydaycalculator.ui.workorder.workorder.adapter.WorkOrderLookupAdapter

//private const val TAG = FRAG_WORK_ORDER_LOOKUP

class WorkOrderLookupFragment :
    Fragment(R.layout.fragment_work_order_view) {

    private var _binding: FragmentWorkOrderViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private var curEmployer: Employers? = null
    private var workOrderAdapter: WorkOrderLookupAdapter? = null

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
        mainActivity.title = getString(R.string.choose_a_work_order)
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
            curEmployer = mainActivity.mainViewModel.getEmployer()
            tvEmployer.text = curEmployer?.employerName
            populateWorkOrders(curEmployer!!.employerId)
        }
    }

    private fun populateWorkOrders(employerId: Long) {
        workOrderAdapter = WorkOrderLookupAdapter(
            mainActivity, mView
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

    private fun setClickActions() {
        binding.apply {
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}