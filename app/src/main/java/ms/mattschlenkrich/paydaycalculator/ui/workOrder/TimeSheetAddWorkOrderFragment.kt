package ms.mattschlenkrich.paydaycalculator.ui.workOrder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentTimeSheetAddWorkOrderBinding
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class TimeSheetAddWorkOrderFragment : Fragment(R.layout.fragment_time_sheet_add_work_order) {

    private var _binding: FragmentTimeSheetAddWorkOrderBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private lateinit var workOrderList: ArrayList<String>
    private lateinit var workDateObject: WorkDates
    private lateinit var curEmployer: Employers

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimeSheetAddWorkOrderBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.add_time_to_work_order)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInfoValues()
        populateWorkOrderList()
        setClickActions()
    }

    private fun setInfoValues() {
        if (mainActivity.mainViewModel.getWorkDateObject() != null) {
            workDateObject = mainActivity.mainViewModel.getWorkDateObject()!!
            binding.apply {
                lblDate.text = df.getDisplayDate(workDateObject.wdDate)
                if (mainActivity.mainViewModel.getEmployerString() != null) {
                    spEmployers.visibility = View.INVISIBLE
                    tvEmployers.visibility = View.VISIBLE
                    tvEmployers.text = mainActivity.mainViewModel.getEmployerString()
                }
            }
        }
    }

    private fun populateWorkOrderList() {
        workOrderList = getWorkOrderList()
        binding.apply {
            val woAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_normal, workOrderList
            )
            acWorkOrder.setAdapter(woAdapter)
        }
    }

    private fun getWorkOrderList(): ArrayList<String> {
        val newList = ArrayList<String>()
        mainActivity.workOrderViewModel.getWorkOrdersByEmployerId(
            workDateObject.wdEmployerId
        ).observe(viewLifecycleOwner) { list ->
            list.listIterator().forEach {
                newList.add(it.workOrderId.toString())
            }
        }
        return newList
    }

    private fun setClickActions() {
        binding.apply {
            fabDone.setOnClickListener {
                validateWorkOrderEntry()
            }
        }
    }

    private fun validateWorkOrderEntry() {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}