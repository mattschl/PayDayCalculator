package ms.mattschlenkrich.paydaycalculator.ui.workOrder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentTimeSheetAddWorkOrderBinding
import ms.mattschlenkrich.paydaycalculator.model.workOrder.WorkOrder
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class TimeSheetAddWorkOrderFragment : Fragment(R.layout.fragment_time_sheet_add_work_order) {

    private var _binding: FragmentTimeSheetAddWorkOrderBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private val workOrderList = ArrayList<WorkOrder>()

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
        setDefaultValues()
        populateWorkOrderList()
        setClickActions()
    }

    private fun setDefaultValues() {
        TODO("Not yet implemented")
    }

    private fun populateWorkOrderList() {
        getWorkOrderList()
        TODO("Not yet implemented")
    }

    private fun getWorkOrderList() {
        TODO("Not yet implemented")
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