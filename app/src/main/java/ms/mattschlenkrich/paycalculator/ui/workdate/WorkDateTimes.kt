package ms.mattschlenkrich.paycalculator.ui.workdate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkDateTimeBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import java.util.Calendar

class WorkDateTimes : Fragment(R.layout.fragment_work_date_time) {

    private var _binding: FragmentWorkDateTimeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private lateinit var curDate: WorkDates
    private lateinit var existingHistories: List<WorkOrderHistoryTimeWorkedCombined>
    private lateinit var startTime: Calendar
    private lateinit var endTime: Calendar
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private val defaultScope = CoroutineScope(Dispatchers.Default)
    private val mainScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkDateTimeBinding.inflate(inflater, container, false)
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel
        startTime = Calendar.getInstance()
        endTime = Calendar.getInstance()
        mainActivity.title = getString(R.string.enter_work_time)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateVariablesAndValues()
        setClickActions()
    }

    private fun populateVariablesAndValues() {
        if (mainViewModel.getWorkDateObject() != null) {
            curDate = mainViewModel.getWorkDateObject()!!
            workOrderViewModel.getTimeWorkedPerDay(curDate.workDateId)
                .observe(viewLifecycleOwner) { histories ->
                    existingHistories = histories

                }
        }
    }

    private fun setClickActions() {
        TODO("Not yet implemented")
    }


    override fun onDestroy() {
        mainScope.cancel()
        defaultScope.cancel()
        super.onDestroy()
        _binding = null
    }

}