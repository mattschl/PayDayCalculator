package ms.mattschlenkrich.paycalculator.ui.workorder.workorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.database.model.workorder.Areas
import ms.mattschlenkrich.paycalculator.database.model.workorder.JobSpec
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrder
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderJobSpec
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderJobSpecCombined
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkOrderJobSpecUpdateBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity

class WorkOrderJobSpecUpdateFragment :
    Fragment(R.layout.fragment_work_order_job_spec_update) {

    private var _binding: FragmentWorkOrderJobSpecUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private lateinit var workOrder: WorkOrder
    private lateinit var originalJobSpec: WorkOrderJobSpecCombined
    private lateinit var jobSpecListForAutoComplete: List<JobSpec>
    private lateinit var areaListForAutoComplete: List<Areas>
    private var curJobSpec: JobSpec? = null
    private var curArea: Areas? = null
    private val nf = NumberFunctions()
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkOrderJobSpecUpdateBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel
        mainActivity.title = getString(R.string.edit_job_spec)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateInitialValues()
        setClickActions()
    }

    private fun populateInitialValues() {
        CoroutineScope(Dispatchers.Default).launch {
            populateJobSpecListForAutoComplete()
            populateAreaListForAutoComplete()
            populateJobSpecValues()
            populateWorkOrderInfo()
        }
    }

    private suspend fun populateJobSpecValues() =
        withContext(Dispatchers.Main) {
            if (mainViewModel.getWorkOrderJobSpecId() != null) {
                workOrderViewModel.getWorkOrderJobSpec(
                    mainViewModel.getWorkOrderJobSpecId()!!
                ).observe(viewLifecycleOwner) { jobSpec ->
                    originalJobSpec = jobSpec
                    binding.apply {
                        var display = getString(R.string.original_job_spec) +
                                jobSpec.jobSpec.jsName
                        lblJobSpec.text = display
                        acJobSpec.setText(jobSpec.jobSpec.jsName)
                        display = if (jobSpec.workOrderJobSpec.wojsAreaId != null) {
                            getString(R.string.old_area_of_work) +
                                    jobSpec.area?.areaName
                        } else {
                            getString(R.string.no_area_was_indicated)
                        }
                        lblArea.text = display
                        acArea.setText(jobSpec.area?.areaName)
                        if (jobSpec.workOrderJobSpec.wojsNote != null) {
                            etNote.setText(jobSpec.workOrderJobSpec.wojsNote)
                        }
                    }
                }
            }
        }

    private suspend fun populateJobSpecListForAutoComplete() =
        withContext(Dispatchers.Main) {
            workOrderViewModel.getJobSpecsAll().observe(viewLifecycleOwner) { list ->
                jobSpecListForAutoComplete = list
                val jobSpecNames = ArrayList<String>()
                list.listIterator().forEach { jobSpecNames.add(it.jsName) }
                val jsAdapter = ArrayAdapter(
                    mView.context,
                    R.layout.spinner_item_normal,
                    jobSpecNames
                )
                binding.acJobSpec.setAdapter(jsAdapter)
            }
        }

    private suspend fun populateAreaListForAutoComplete() =
        withContext(Dispatchers.Main) {
            workOrderViewModel.getAreasList().observe(viewLifecycleOwner) { list ->
                areaListForAutoComplete = list
                val areaNames = ArrayList<String>()
                list.listIterator().forEach { areaNames.add(it.areaName) }
                val areaAdapter = ArrayAdapter(
                    mView.context,
                    R.layout.spinner_item_normal,
                    areaNames
                )
                binding.acArea.setAdapter(areaAdapter)
            }
        }

    private suspend fun populateWorkOrderInfo() =
        withContext(Dispatchers.Main) {
            if (mainViewModel.getWorkOrder() != null) {
                workOrder = mainViewModel.getWorkOrder()!!
                val display = getString(R.string.edit_the_job_spec_for_wo_) + workOrder.woNumber +
                        getString(R.string._at_) + " " + workOrder.woAddress +
                        "\n" + workOrder.woDescription
                binding.tvInfo.text = display
            }
        }

    private fun setCurJobSpec(): Boolean {
        curJobSpec = null
        binding.apply {
            if (!acJobSpec.text.isNullOrBlank()) {
                for (jobSpec in jobSpecListForAutoComplete) {
                    if (acJobSpec.text.toString().trim() ==
                        jobSpec.jsName
                    ) {
                        curJobSpec = jobSpec
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun setCurArea(): Boolean {
        curArea = null
        binding.apply {
            if (!acArea.text.isNullOrBlank()) {
                for (area in areaListForAutoComplete) {
                    if (acArea.text.toString().trim() ==
                        area.areaName
                    ) {
                        curArea = area
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun setClickActions() {
        binding.apply {
            fabDone.setOnClickListener {
                updateJobSpecInWorkOrderIfValid()
            }
        }
    }

    private fun updateJobSpecInWorkOrderIfValid() {
        val message = validateOrAddJobSpecToDbAndUpdateWithArea()
        if (message != ANSWER_OK) {
            showMessage(getString(R.string.error_) + message)
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(mView.context, message, Toast.LENGTH_LONG).show()
    }

    private fun validateOrAddJobSpecToDbAndUpdateWithArea(): String {
        binding.apply {
            if (setCurJobSpec()) {
                updateJobSpecInWorkOrderAndAddDetails(curJobSpec!!.jobSpecId)
                return ANSWER_OK
            } else if (!acJobSpec.text.isNullOrBlank()) {
                insertJobSpecIntoDbAndContinueToUpdate()
                return ANSWER_OK
            }
        }
        return getString(R.string.please_enter_a_valid_job_spec)
    }

    private fun insertJobSpecIntoDbAndContinueToUpdate() {
        CoroutineScope(Dispatchers.Default).launch {
            val jobSpecId = async {
                insertJobSpecIntoDb(binding.acJobSpec.text.toString().trim())
            }
            updateJobSpecInWorkOrderAndAddDetails(jobSpecId.await())
        }
    }

    private fun insertJobSpecIntoDb(jobSpecName: String): Long {
        val newJobSpec = JobSpec(
            nf.generateRandomIdAsLong(), jobSpecName, false, df.getCurrentTimeAsString()
        )
        workOrderViewModel.insertJobSpec(newJobSpec)
        return newJobSpec.jobSpecId
    }

    private fun updateJobSpecInWorkOrderAndAddDetails(jobSpecId: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_250)
            binding.apply {
                if (acArea.text.isNullOrBlank()) {
                    curArea = null
                    updateWorkOrderJobSpecAndGotoCallingFragment(jobSpecId, null)
                } else if (setCurArea()) {
                    updateWorkOrderJobSpecAndGotoCallingFragment(jobSpecId, curArea?.areaId)
                } else if (!acArea.text.isNullOrBlank()) {
                    val newAreaId = async { insertAreaIntoDb(acArea.text.toString().trim()) }
                    updateWorkOrderJobSpecAndGotoCallingFragment(jobSpecId, newAreaId.await())
                }
            }
        }
    }

    private fun insertAreaIntoDb(areaName: String): Long {
        val newArea = Areas(
            nf.generateRandomIdAsLong(),
            areaName,
            false,
            df.getCurrentTimeAsString()
        )
        workOrderViewModel.insertArea(newArea)
        return newArea.areaId
    }

    private fun updateWorkOrderJobSpecAndGotoCallingFragment(jobSpecId: Long, areaId: Long?) {
        CoroutineScope(Dispatchers.Main).launch {
            val note: String? = getNote()
            delay(WAIT_250)
            originalJobSpec.workOrderJobSpec.apply {
                workOrderViewModel.updateWorkOrderJobSpec(
                    WorkOrderJobSpec(
                        workOrderJobSpecId,
                        wojsWorkOrderId,
                        jobSpecId,
                        areaId,
                        note,
                        wojsSequence,
                        false,
                        df.getCurrentTimeAsString()
                    )
                )
            }
            gotoCallingFragment()
        }
    }

    private fun getNote(): String? {
        binding.apply {
            return if (etNote.text.isNullOrBlank()) {
                null
            } else {
                etNote.text.toString().trim()
            }
        }
    }

    private fun gotoCallingFragment() {
        mainViewModel.setJobSpec(null)
        gotoWorkOrderUpdateFragment()
    }


    private fun gotoWorkOrderUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderJobSpecUpdateFragmentDirections
                .actionWorkOrderJobSpecUpdateFragmentToWorkOrderUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}