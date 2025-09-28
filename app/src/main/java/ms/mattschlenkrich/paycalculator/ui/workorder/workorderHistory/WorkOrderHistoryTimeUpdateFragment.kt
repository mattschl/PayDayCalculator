package ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkOrderHistoryTimeBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import java.util.Calendar

class WorkOrderHistoryTimeUpdateFragment :
    Fragment(R.layout.fragment_work_order_history_time) {
    private var _binding: FragmentWorkOrderHistoryTimeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private lateinit var startTime: Calendar
    private lateinit var endTime: Calendar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWorkOrderHistoryTimeBinding.inflate(inflater, container, false)
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel
        startTime = Calendar.getInstance()
        endTime = Calendar.getInstance()
        mainActivity.title = getString(R.string.update_work_time)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateVariablesAndValues()
        setClickActions()
    }

    private fun populateVariablesAndValues() {
        TODO("Not yet implemented")
    }

    private fun setClickActions() {
        TODO("Not yet implemented")
    }
}