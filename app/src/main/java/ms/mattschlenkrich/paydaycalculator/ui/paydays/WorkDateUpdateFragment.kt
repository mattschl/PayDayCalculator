package ms.mattschlenkrich.paydaycalculator.ui.paydays

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.adapter.WorkDateUpdateCustomExtraAdapter
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.WAIT_250
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkDateUpdateBinding
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDateExtras
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

//private const val TAG = "WorkDateUpdate"

class WorkDateUpdateFragment : Fragment(
    R.layout.fragment_work_date_update
) {

    private var _binding: FragmentWorkDateUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var curDate: WorkDates
    private lateinit var curDateString: String
    private val workDateExtras = ArrayList<WorkDateExtras>()
    private val customWorkDateExtras = ArrayList<WorkDateExtras>()
    private val df = DateFunctions()
//    private val cf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkDateUpdateBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = "Update this work date"
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun changeDate() {
        binding.apply {
            val curDateAll = curDateString.split("-")
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, monthOfYear, dayOfMonth ->
                    val month = monthOfYear + 1
                    val display = "$year-${
                        month.toString()
                            .padStart(2, '0')
                    }-${
                        dayOfMonth.toString().padStart(2, '0')
                    }"
                    curDateString = display
                    tvWorkDate.text = df.getDisplayDate(display)

                },
                curDateAll[0].toInt(),
                curDateAll[1].toInt() - 1,
                curDateAll[2].toInt()
            )
            datePickerDialog.setTitle(getString(R.string.choose_a_work_date))
            datePickerDialog.show()
        }
    }

    private fun setClickActions() {
        binding.apply {
            fabDone.setOnClickListener {
                updateWorkDate()
            }
            fabAddExtra.setOnClickListener {
                gotoWorkDateExtraAddFragment()
            }
            tvWorkDate.setOnClickListener {
                changeDate()
            }
        }
    }

    private fun gotoWorkDateExtraAddFragment() {
        mainActivity.mainViewModel.setWorkDateObject(getCurrentWorkDate())
        mainActivity.mainViewModel.setWorkDateExtraList(workDateExtras)
        mView.findNavController().navigate(
            WorkDateUpdateFragmentDirections
                .actionWorkDateUpdateFragmentToWorkDateExtraAddFragment()
        )
    }

    private fun updateWorkDate() {
        binding.apply {
            mainActivity.payDayViewModel.updateWorkDate(
                getCurrentWorkDate()
            )
        }
        gotoTimeSheetFragment()
    }

    private fun gotoTimeSheetFragment() {
        mView.findNavController().navigate(
            WorkDateUpdateFragmentDirections
                .actionWorkDateUpdateFragmentToTimeSheetFragment()
        )
    }

    private fun populateValues() {
        if (mainActivity.mainViewModel.getWorkDateObject() != null) {
            curDate = mainActivity.mainViewModel.getWorkDateObject()!!
            curDateString = curDate.wdDate
            mainActivity.mainViewModel.setWorkDateString(
                curDateString
            )
            binding.apply {
                tvWorkDate.text = df.getDisplayDate(curDate.wdDate)
                etHours.setText(curDate.wdRegHours.toString())
                etOt.setText(curDate.wdOtHours.toString())
                etDblOt.setText(curDate.wdDblOtHours.toString())
                etStat.setText(curDate.wdStatHours.toString())
            }
            populateExtras()
        }
    }

    private fun getCurrentWorkDate(): WorkDates {
        binding.apply {
            return WorkDates(
                curDate.workDateId,
                curDate.wdPayPeriodId,
                curDate.wdEmployerId,
                curDate.wdCutoffDate,
                curDateString,
                if (etHours.text.isNullOrBlank()) 0.0 else etHours.text.toString().trim()
                    .toDouble(),
                if (etOt.text.isNullOrBlank()) 0.0 else etOt.text.toString().trim()
                    .toDouble(),
                if (etDblOt.text.isNullOrBlank()) 0.0 else etDblOt.text.toString().trim()
                    .toDouble(),
                if (etStat.text.isNullOrBlank()) 0.0 else etStat.text.toString().trim()
                    .toDouble(),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    fun populateExtras() {
        val currentWorkDateObject =
            mainActivity.mainViewModel.getWorkDateObject()!!
        activity?.let {
            mainActivity.payDayViewModel.getWorkDateExtras(curDate.workDateId)
                .observe(viewLifecycleOwner) { extras ->
                    workDateExtras.clear()
                    customWorkDateExtras.clear()
                    extras.listIterator().forEach {
                        workDateExtras.add(it)
                        customWorkDateExtras.add(it)

                    }
                }
        }
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_250)
            binding.apply {
                activity?.let {
                    mainActivity.workExtraViewModel.getExtraTypesAndDefByDaily(
                        currentWorkDateObject.wdEmployerId,
                        currentWorkDateObject.wdCutoffDate
                    ).observe(viewLifecycleOwner) { extras ->
                        extras.listIterator().forEach {
                            val tempExtra = WorkDateExtras(
                                0,
                                currentWorkDateObject.workDateId,
                                null,
                                it.extraType.wetName,
                                it.extraType.wetAppliesTo,
                                it.extraType.wetAttachTo,
                                it.definition.weValue,
                                it.definition.weIsFixed,
                                it.extraType.wetIsCredit,
                                true,
                                df.getCurrentTimeAsString()
                            )
                            var found = false
                            for (oldExtra in workDateExtras) {
                                if (oldExtra.wdeName == it.extraType.wetName) {
                                    found = true
                                    break
                                }
                            }
                            if (!found) {
                                workDateExtras.add(tempExtra)
                            }

                        }
                        workDateExtras.sortBy { extra ->
                            extra.wdeName
                        }
                        val extraAdapter = WorkDateUpdateCustomExtraAdapter(
                            mainActivity, mView,
                            this@WorkDateUpdateFragment,
                            workDateExtras
                        )
                        rvExtras.apply {
                            layoutManager = LinearLayoutManager(mView.context)
                            adapter = extraAdapter
                        }
                    }

                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}