package ms.mattschlenkrich.paycalculator.ui.workorder.workPerforrmed

import android.app.AlertDialog
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_PERFORMED_VIEW
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkPerformed
import ms.mattschlenkrich.paycalculator.databinding.FragmentSingleItemUpdateBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity


class WorkPerformedUpdateFragment :
    Fragment(R.layout.fragment_single_item_update) {

    private var _binding: FragmentSingleItemUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()

    private val workPerformedList = ArrayList<WorkPerformed>()
    private lateinit var oldWorkPerformed: WorkPerformed

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSingleItemUpdateBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
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
        if (mainActivity.mainViewModel.getWorkPerformedId() != null) {
            mainActivity.workOrderViewModel.getWorkPerformed(
                mainActivity.mainViewModel.getWorkPerformedId()!!
            ).observe(viewLifecycleOwner) { work ->
                oldWorkPerformed = work
                binding.apply {
                    val display =
                        getString(R.string.update_work_description_) +
                                oldWorkPerformed.wpDescription
                    tvTitle.text = display
                    etItem.setText(oldWorkPerformed.wpDescription)
                }
            }

        }
    }

    private fun populateWorkPerformedListForValidation() {
        mainActivity.workOrderViewModel.getWorkPerformedAll().observe(
            viewLifecycleOwner
        ) { list ->
            workPerformedList.clear()
            for (workPerformed in list.listIterator()) {
                workPerformedList.add(workPerformed)
            }
        }
    }

    private fun setClickActions() {
        binding.apply {
            btnUpdate.setOnClickListener {
                updateWorkPerformedIfValid()
            }
            btnCancel.setOnClickListener {
                gotoCallingFragment()
            }
        }
    }

    private fun updateWorkPerformedIfValid() {
        val answer = validateWorkPerformed()
        if (answer == ANSWER_OK) {
            updateWorkPerformed()
        } else {
            displayError(answer)
        }
    }

    private fun displayError(answer: String) {
        Toast.makeText(
            mView.context,
            getString(R.string.error_) + answer,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun validateWorkPerformed(): String {
        binding.apply {
            if (etItem.text.isNullOrBlank()) {
                return getString(R.string.please_enter_a_valid_work_performed_description)
            }
            for (workPerformed in workPerformedList) {
                if (workPerformed.wpDescription ==
                    etItem.text.toString().trim() &&
                    etItem.text.toString().trim() !=
                    oldWorkPerformed.wpDescription
                ) {
                    return getString(R.string.this_work_performed_description_already_exists)
                }
            }
        }
        return ANSWER_OK
    }

    private fun updateWorkPerformed() {
        try {
            mainActivity.workOrderViewModel.updateWorkPerformed(
                WorkPerformed(
                    oldWorkPerformed.workPerformedId,
                    binding.etItem.text.toString().trim(),
                    false,
                    df.getCurrentTimeAsString()
                )
            )
            gotoCallingFragment()
        } catch (e: SQLiteException) {
            AlertDialog.Builder(mView.context)
                .setTitle(getString(R.string.something_went_wrong))
                .setMessage(
                    getString(R.string.check_to_see_if_this_work_was_already_entered_) +
                            " " + e.toString()
                )
                .setNeutralButton(getString(R.string.ok), null)
                .show()
        }
    }

    private fun gotoCallingFragment() {
        mainActivity.mainViewModel.setWorkPerformedId(null)
        if (mainActivity.mainViewModel.getCallingFragment()!!.contains(
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
            WorkPerformedUpdateFragmentDirections
                .actionWorkPerformedUpdateFragmentToWorkPerformedViewFragment()
        )
    }

    private fun gotoWorkPerformedUpdateFragment() {
        mView.findNavController().navigate(
            WorkPerformedUpdateFragmentDirections
                .actionWorkPerformedUpdateFragmentToWorkOrderHistoryUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}