package ms.mattschlenkrich.paycalculator.ui.workorder.workPerformed

import android.app.AlertDialog
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.ExceptionUnknown
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_PERFORMED_UPDATE
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_PERFORMED_VIEW
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkPerformed
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentSingleItemUpdateBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity

private const val TAG = FRAG_WORK_PERFORMED_UPDATE

class WorkPerformedUpdateFragment : Fragment(R.layout.fragment_single_item_update) {

    private var _binding: FragmentSingleItemUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private val df = DateFunctions()
    private val workPerformedList = ArrayList<WorkPerformed>()
    private lateinit var oldWorkPerformed: WorkPerformed
    private val mainScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSingleItemUpdateBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel
        mainActivity.title = getString(R.string.update_work_performed_description)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setValues()
        setClickActions()
    }

    private fun setValues() {
        populateWorkPerformedListForValidation()
        if (mainViewModel.getWorkPerformedId() != null) {
            workOrderViewModel.getWorkPerformed(mainViewModel.getWorkPerformedId()!!)
                .observe(viewLifecycleOwner) { work ->
                    oldWorkPerformed = work
                    binding.apply {
                        var display =
                            getString(R.string.update_work_description_) + oldWorkPerformed.wpDescription
                        tvTitle.text = display
                        etItem.setText(oldWorkPerformed.wpDescription)
                        display = "Merge " + oldWorkPerformed.wpDescription
                        btnUpdate.text = display
                    }
                }

        }
    }

    private fun populateWorkPerformedListForValidation() {
        workOrderViewModel.getWorkPerformedAll().observe(viewLifecycleOwner) { list ->
            workPerformedList.clear()
            for (workPerformed in list.listIterator()) {
                workPerformedList.add(workPerformed)
            }
        }
    }

    private fun setClickActions() {
        binding.apply {
            btnUpdate.setOnClickListener { updateWorkPerformedIfValid(true) }
            btnCancel.setOnClickListener { gotoCallingFragment() }
            btnMerge.setOnClickListener { chooseMergeOptions() }
        }
    }

    private fun chooseMergeOptions() {
        AlertDialog.Builder(mView.context)
            .setTitle(
                getString(
                    R.string.choose_merge_option_for,
                    binding.etItem.text.toString().trim()
                )
            )
            .setItems(
                arrayOf(
                    "Make this a master description and add children",
                    "Add this to another description as a child",
                    "*Note: This will attempt to save the current Work Performed description."
                )
            ) { _, pos ->
                when (pos) {
                    0 -> {
                        setOptionsForMergeAndGotoMerge(true)
                    }

                    1 -> {
                        setOptionsForMergeAndGotoMerge(false)
                    }
                }
            }
            .setNeutralButton(getString(R.string.cancel), null)
            .show()
    }

    private fun setOptionsForMergeAndGotoMerge(isMaster: Boolean) {
        mainScope.launch {
            try {
                updateWorkPerformedIfValid(false)
                mainViewModel.setWorkPerformedId(oldWorkPerformed.workPerformedId)
                mainViewModel.setWorkPerformedIsMaster(isMaster)
                mainViewModel.addCallingFragment(TAG)
                gotoWorkPerformedMergeFragment()
            } catch (e: ExceptionUnknown) {
                Log.d(TAG, "exception is ${e.toString()}")
            }

        }
    }

    private fun gotoWorkPerformedMergeFragment() {
        mView.findNavController().navigate(
            WorkPerformedUpdateFragmentDirections.actionWorkPerformedUpdateFragmentToWorkPerformedMergeFragment()
        )
    }

    private fun updateWorkPerformedIfValid(gotoCallingFragment: Boolean) {
        val answer = validateWorkPerformed()
        if (answer == ANSWER_OK) {
            updateWorkPerformed(gotoCallingFragment)
        } else {
            displayError(answer)
        }
    }

    private fun displayError(answer: String) {
        Toast.makeText(mView.context, getString(R.string.error_) + answer, Toast.LENGTH_LONG).show()
    }

    private fun validateWorkPerformed(): String {
        binding.apply {
            if (etItem.text.isNullOrBlank()) {
                return getString(R.string.please_enter_a_valid_work_performed_description)
            }
            for (workPerformed in workPerformedList) {
                if (workPerformed.wpDescription == etItem.text.toString()
                        .trim() && etItem.text.toString().trim() != oldWorkPerformed.wpDescription
                ) {
                    return getString(R.string.this_work_performed_description_already_exists)
                }
            }
        }
        return ANSWER_OK
    }

    private fun updateWorkPerformed(gotoCallingFragment: Boolean) {
        try {
            workOrderViewModel.updateWorkPerformed(
                WorkPerformed(
                    oldWorkPerformed.workPerformedId,
                    binding.etItem.text.toString().trim(),
                    false,
                    df.getCurrentTimeAsString()
                )
            )
            if (gotoCallingFragment) gotoCallingFragment()
        } catch (e: SQLiteConstraintException) {
            AlertDialog.Builder(mView.context).setTitle(getString(R.string.something_went_wrong))
                .setMessage(
                    getString(R.string.check_to_see_if_this_work_was_already_entered_) + " " + e.toString()
                ).setNeutralButton(getString(R.string.ok), null).show()
            throw ExceptionUnknown("Could not complete action")
        }
    }

    private fun gotoCallingFragment() {
        mainViewModel.setWorkPerformedId(null)
        if (mainViewModel.getCallingFragment()!!.contains(
                FRAG_WORK_PERFORMED_VIEW
            )
        ) {
            gotoWorkPerformedViewFragment()
        } else {
            gotoWorkPerformedUpdateFragment()
        }
    }

    private fun gotoWorkPerformedViewFragment() {
        mView.findNavController().navigate(
            WorkPerformedUpdateFragmentDirections.actionWorkPerformedUpdateFragmentToWorkPerformedViewFragment()
        )
    }

    private fun gotoWorkPerformedUpdateFragment() {
        mView.findNavController().navigate(
            WorkPerformedUpdateFragmentDirections.actionWorkPerformedUpdateFragmentToWorkOrderHistoryUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}