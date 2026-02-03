package ms.mattschlenkrich.paycalculator.ui.workorder.workPerformed

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_PERFORMED_MERGE
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_500
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkPerformed
import ms.mattschlenkrich.paycalculator.database.model.workorder.merged.WorkPerformedMerged
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentEntityMergeBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.workorder.workPerformed.adapter.WorkPerformedChildrenAdapter
import ms.mattschlenkrich.paycalculator.ui.workorder.workPerformed.adapter.WorkPerformedMergedAdapter

private const val TAG = FRAG_WORK_PERFORMED_MERGE

class WorkPerformedMergeFragment : Fragment(R.layout.fragment_entity_merge) {

    private var _binding: FragmentEntityMergeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private lateinit var workPerformedList: List<WorkPerformed>
    private lateinit var wpParent: WorkPerformed
    private lateinit var wpChild: WorkPerformed
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val df = DateFunctions()
    private val nf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEntityMergeBinding.inflate(inflater, container, false)
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel
        mainActivity.topMenuBar.title = getString(R.string.merge_work_performed)
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
            val wpDescriptions = list.map { it.wpDescription }
            val wpDescriptionAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_normal, wpDescriptions
            )
            val listAdapter = WorkPerformedMergedAdapter(mainActivity, mView, this)
            listAdapter.differ.submitList(list)
            binding.apply {
                acParent.setAdapter(wpDescriptionAdapter)
                acChild.setAdapter(wpDescriptionAdapter)
                rvMergedList.layoutManager = LinearLayoutManager(mView.context)
                rvMergedList.adapter = listAdapter
            }

        }
    }

    private fun populateFromCache() {
        mainViewModel.apply {
            if (getWorkPerformedId() != null) {
                workOrderViewModel.getWorkPerformed(getWorkPerformedId()!!)
                    .observe(viewLifecycleOwner) { workPerformed ->
                        if (getWorkPerformedIsMaster()) {
                            chooseToMergeAsParent(workPerformed)
                        } else {
                            chooseToMergeAsChild(workPerformed)
                        }
                    }
            }
        }
    }

    private fun setClickActions() {
        binding.apply {
            acParent.setOnItemClickListener { _, _, _, _ ->
                chooseToMergeAsParent(workPerformedList.first { it.wpDescription == acParent.text.toString() })
            }
            acChild.setOnItemClickListener { _, _, _, _ ->
                chooseToMergeAsChild(workPerformedList.first { it.wpDescription == acChild.text.toString() })
            }
            btnMerge.setOnClickListener { chooseOptionsForMerge() }
            btnDone.setOnClickListener { gotoCallingFragment() }

        }
    }

    private fun chooseOptionsForMerge() {
        AlertDialog.Builder(mView.context)
            .setTitle(getString(R.string.choose_merge_option))
            .setMessage(getString(R.string.either_merge_and_replace_or_merge_))
            .setPositiveButton(getString(R.string.merge_and_replace)) { _, _ ->
                mergeAndReplace()
            }
            .setNeutralButton(getString(R.string.merge_and_keep)) { _, _ ->
                mergeAndKeep()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun mergeAndReplace() {
        mainScope.launch {
            workOrderViewModel.updateWorkPerformedMerged(
                wpChild.workPerformedId,
                wpParent.workPerformedId
            )
            delay(WAIT_500)
            try {
                workOrderViewModel.deleteWorkPerformedMerged(
                    wpChild.workPerformedId,
                )
                workOrderViewModel.deleteWorkPerformed(wpChild)
            } catch (e: Exception) {
                Log.d(TAG, e.message.toString())
                delay(WAIT_500)
                workOrderViewModel.deleteWorkPerformedMerged(
                    wpChild.workPerformedId,
                )
                workOrderViewModel.deleteWorkPerformed(wpChild)
            } finally {
                populateChildren(wpParent)
                binding.acChild.setText("")
            }
        }
    }

    private fun mergeAndKeep() {
        workOrderViewModel.insertWorkPerformedMerged(
            WorkPerformedMerged(
                nf.generateRandomIdAsLong(),
                wpParent.workPerformedId,
                wpChild.workPerformedId,
                false,
                df.getCurrentTimeAsString()
            )
        )
        populateChildren(wpParent)
    }

    fun chooseToMergeAsParent(workPerformed: WorkPerformed) {
        binding.apply {
            wpParent = workPerformed
            acParent.setText(workPerformed.wpDescription)
            populateChildren(workPerformed)
        }
    }

    private fun populateChildren(workPerformed: WorkPerformed) {
        binding.apply {
            workOrderViewModel.getWorkPerformedChildren(workPerformed.workPerformedId)
                .observe(viewLifecycleOwner) { list ->
                    if (list.isNotEmpty()) {
                        crdChildren.visibility = View.VISIBLE
                        val childAdapter =
                            WorkPerformedChildrenAdapter(mView, this@WorkPerformedMergeFragment)
                        childAdapter.differ.submitList(list)
                        rvChildren.apply {
                            layoutManager = LinearLayoutManager(mView.context)
                            adapter = childAdapter
                        }
                    } else {
                        crdChildren.visibility = View.GONE
                    }
                }
        }
    }

    fun chooseToMergeAsChild(workPerformed: WorkPerformed) {
        binding.apply {
            wpChild = workPerformed
            acChild.setText(workPerformed.wpDescription)
        }
    }

    private fun gotoCallingFragment() {
        gotoWorkPerformedUpdateFragment()
    }

    fun gotoWorkPerformedUpdateFragment() {
        mView.findNavController().navigate(
            WorkPerformedMergeFragmentDirections.actionWorkPerformedMergeFragmentToWorkPerformedUpdateFragment()
        )
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}