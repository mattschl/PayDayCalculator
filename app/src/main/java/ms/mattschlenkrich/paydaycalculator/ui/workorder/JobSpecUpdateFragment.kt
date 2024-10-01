package ms.mattschlenkrich.paydaycalculator.ui.workorder

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
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.JobSpec
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentJobSpecBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity


class JobSpecUpdateFragment : Fragment(R.layout.fragment_job_spec) {

    private var _binding: FragmentJobSpecBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity

    private val jobSpecList = ArrayList<JobSpec>()
    private lateinit var oldJobSpec: JobSpec
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJobSpecBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = "Update Job Spec"
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fillJobSpecListForValidation()
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

    private fun updateIfValid() {
        val answer = validateJobSpec()
        if (answer == ANSWER_OK) {
            updateJobSpec()
        } else {
            Toast.makeText(
                mView.context,
                answer, Toast.LENGTH_LONG
            ).show()
        }

    }

    private fun updateJobSpec() {
        mainActivity.workOrderViewModel.updateJobSpec(
            JobSpec(
                oldJobSpec.jobSpecId,
                binding.etJobSpec.text.toString().trim(),
                false,
                df.getCurrentTimeAsString()
            )
        )
        gotoCallingFragment()
    }

    private fun validateJobSpec(): String {
        binding.apply {
            if (etJobSpec.text.isNullOrBlank()) {
                return "    ERROR!!/n" +
                        "Please enter a valid Job Spec!"
            }
            for (jobSpec in jobSpecList) {
                if (jobSpec.jsName ==
                    binding.etJobSpec.text.toString().trim()
                ) {
                    return "   ERROR!/n" +
                            "This job spec already exists!"
                }
            }
            return ANSWER_OK
        }
    }

    private fun gotoCallingFragment() {
        mainActivity.mainViewModel.setJobSpec(null)
        mView.findNavController().navigate(
            JobSpecUpdateFragmentDirections
                .actionJobSpecUpdateFragmentToWorkOrderUpdateFragment()
        )
    }

    private fun fillJobSpecListForValidation() {
        mainActivity.workOrderViewModel.getJobSpecsAll().observe(
            viewLifecycleOwner
        ) { list ->
            jobSpecList.clear()
            list.listIterator().forEach {
                jobSpecList.add(it)
            }
        }
    }

    private fun setView() {
        if (mainActivity.mainViewModel.getJobSpec() != null) {
            oldJobSpec =
                mainActivity.mainViewModel.getJobSpec()!!
            binding.apply {
                val display =
                    "Update: ${oldJobSpec.jsName}"
                tvTitle.text = display
                etJobSpec.setText(oldJobSpec.jsName)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}