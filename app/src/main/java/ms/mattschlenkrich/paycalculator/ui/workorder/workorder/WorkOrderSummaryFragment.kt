package ms.mattschlenkrich.paycalculator.ui.workorder.workorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkOrderSummaryBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity

class WorkOrderSummaryFragment :
    Fragment(R.layout.fragment_work_order_summary) {

    private var _binding: FragmentWorkOrderSummaryBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
//    private val df = DateFunctions()
//    private val nf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkOrderSummaryBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.work_order_summary)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getValues()
    }

    private fun getValues() {
        // TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}