package ms.mattschlenkrich.paydaycalculator.ui.workOrder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkPerformed
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkPerformedUpdateBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity


class WorkPerformedUpdateFragment :
    Fragment(R.layout.fragment_work_performed_update) {

    private var _binding: FragmentWorkPerformedUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity

    private val workPerformedList = ArrayList<WorkPerformed>()
    private lateinit var oldWorkPerformed: WorkPerformed
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkPerformedUpdateBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = "Update Work Performed description"
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fillWorkPerformedListForValidation()
        setView()
        setClickActions()
    }

    private fun setClickActions() {
        binding.apply {
            btnUpdate.setOnClickListener {
                updateIfValid()
            }
            btnCancel.setOnClickListener {
                gotoCallingFragment()
            }
        }
    }

    private fun gotoCallingFragment() {
        mainActivity.mainViewModel.setWorkPerformed(null)
        mView.findNavController().navigate(
            WorkPerformedUpdateFragmentDirections
                .actionWorkPerformedUpdateFragmentToWorkOrderHistoryUpdateFragment()
        )
    }

    private fun updateIfValid() {
        val answer = validateWorkPerformed()
        if (answer == ANSWER_OK) {
            updateWorkPerformed()
        } else {
            Toast.makeText(
                mView.context,
                answer, Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateWorkPerformed() {
        mainActivity.workOrderViewModel.updateWorkPerformed(
            WorkPerformed(
                oldWorkPerformed.workPerformedId,
                binding.etWorkPerformed.text.toString().trim(),
                false,
                df.getCurrentTimeAsString()
            )
        )
        gotoCallingFragment()
    }

    private fun validateWorkPerformed(): String {
        binding.apply {
            if (etWorkPerformed.text.isNullOrBlank()) {
                return "    ERROR!!/n" +
                        "Please enter a valid Work Performed Description!"
            }
            for (workPerformed in workPerformedList) {
                if (workPerformed.wpDescription ==
                    etWorkPerformed.text.toString().trim()
                ) {
                    return "   ERROR!/n" +
                            "This Work Performed description already exists!"
                }
            }
        }
        return ANSWER_OK
    }

    private fun setView() {
        if (mainActivity.mainViewModel.getWorkPerformed() != null) {
            oldWorkPerformed = mainActivity.mainViewModel.getWorkPerformed()!!
        }
        binding.apply {
            val display =
                "Update: ${oldWorkPerformed.wpDescription}"
            tvTitle.text = display
            etWorkPerformed.setText(oldWorkPerformed.wpDescription)
        }
    }

    private fun fillWorkPerformedListForValidation() {
        mainActivity.workOrderViewModel.getWorkPerformedAll().observe(
            viewLifecycleOwner
        ) { list ->
            workPerformedList.clear()
            for (workPerformed in list.listIterator()) {
                workPerformedList.add(workPerformed)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}