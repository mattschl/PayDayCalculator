package ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkOrderHistoryWorkPerformedUpdateBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity


class WorkOrderHistoryWorkPerformedUpdateFragment :
    Fragment(R.layout.fragment_work_order_history_work_performed_update) {

    private var _binding: FragmentWorkOrderHistoryWorkPerformedUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkOrderHistoryWorkPerformedUpdateBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.edit_the_work_performed)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateInitialValues()
        setClickActions()
    }

    private fun populateInitialValues() {
//        TODO("Not yet implemented")
    }

    private fun setClickActions() {
//        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}