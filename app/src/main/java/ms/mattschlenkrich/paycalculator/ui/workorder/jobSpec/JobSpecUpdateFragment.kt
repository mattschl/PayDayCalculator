package ms.mattschlenkrich.paycalculator.ui.workorder.jobSpec

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
import ms.mattschlenkrich.paycalculator.common.FRAG_JOB_SPEC_VIEW
import ms.mattschlenkrich.paycalculator.database.model.workorder.JobSpec
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentJobSpecBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity


class JobSpecUpdateFragment : Fragment(R.layout.fragment_job_spec) {

    private var _binding: FragmentJobSpecBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel

    private val jobSpecList = ArrayList<JobSpec>()
    private lateinit var oldJobSpec: JobSpec
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJobSpecBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel
        mainActivity.title = getString(R.string.update_job_spec)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setValues()
        setClickActions()
    }

    private fun setValues() {
        populateJobSpecListForValidation()
        if (mainViewModel.getJobSpec() != null) {
            oldJobSpec = mainViewModel.getJobSpec()!!
            binding.apply {
                val display = getString(R.string.update_) + oldJobSpec.jsName
                tvTitle.text = display
                etJobSpec.setText(oldJobSpec.jsName)
            }
        }
    }

    private fun populateJobSpecListForValidation() {
        workOrderViewModel.getJobSpecsAll().observe(
            viewLifecycleOwner
        ) { list ->
            jobSpecList.clear()
            list.listIterator().forEach {
                jobSpecList.add(it)
            }
        }
    }

    private fun setClickActions() {
        binding.apply {
            btnUpdate.setOnClickListener {
                updateJobSpecIfValid()
            }
            btnCancel.setOnClickListener {
                gotoCallingFragment()
            }
        }
    }

    private fun updateJobSpecIfValid() {
        val answer = validateJobSpec()
        if (answer == ANSWER_OK) {
            updateJobSpecAndContinue()
        } else {
            displayMessage(getString(R.string.error_) + answer)
        }

    }

    private fun displayMessage(answer: String) {
        Toast.makeText(mView.context, answer, Toast.LENGTH_LONG).show()
    }

    private fun validateJobSpec(): String {
        binding.apply {
            if (etJobSpec.text.isNullOrBlank()) {
                return getString(R.string.please_enter_a_valid_job_spec)
            }
            for (jobSpec in jobSpecList) {
                if (jobSpec.jsName == binding.etJobSpec.text.toString()
                        .trim() && jobSpec.jsName != oldJobSpec.jsName
                ) {
                    return getString(R.string.this_job_spec_already_exists)
                }
            }
            return ANSWER_OK
        }
    }

    private fun updateJobSpecAndContinue() {
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

    private fun gotoCallingFragment() {
        mainViewModel.apply {
            setJobSpec(null)
            if (getCallingFragment()!!.contains(
                    FRAG_JOB_SPEC_VIEW
                )
            ) {
                gotoJobSpecViewFragment()
            } else {
                gotoWorkOrderUpdateFragment()
            }
        }
    }

    private fun gotoJobSpecViewFragment() {
        mView.findNavController().navigate(
            JobSpecUpdateFragmentDirections.actionJobSpecUpdateFragmentToJobSpecViewFragment()
        )
    }

    private fun gotoWorkOrderUpdateFragment() {
        mView.findNavController().navigate(
            JobSpecUpdateFragmentDirections.actionJobSpecUpdateFragmentToWorkOrderUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}