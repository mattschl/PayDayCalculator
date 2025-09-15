package ms.mattschlenkrich.paycalculator.ui.workorder.workPerformed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_PERFORMED_MERGE
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkPerformed
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentEntityMergeBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity

private const val TAG = FRAG_WORK_PERFORMED_MERGE

class WorkPerformedMergeFragment : Fragment(R.layout.fragment_entity_merge) {

    private var _binding: FragmentEntityMergeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private lateinit var workPerformedList: List<WorkPerformed>
    private val df = DateFunctions()
    private val nf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEntityMergeBinding.inflate(inflater, container, false)
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel
        mainActivity.title = getString(R.string.merge_work_performed)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        populateWorkPerformedLists()
        populateFromCache()
    }

    private fun populateWorkPerformedLists() {
        workOrderViewModel.getWorkPerformedAll().observe(viewLifecycleOwner) { list ->
            workPerformedList = list
            val wpAdapter = ArrayAdapter(
                mView.context, R.layout.list_single_item, list
            )
            binding.apply {
                acMaster.setAdapter(wpAdapter)
                acChild.setAdapter(wpAdapter)
            }
        }
    }

    private fun populateFromCache() {
        TODO("Not yet implemented")
    }

    private fun setClickActions() {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}