package ms.mattschlenkrich.paydaycalculator.ui.paydays

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.adapter.WorkDateUpdateExtraAdapter
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkDateUpdateBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkDateExtras
import ms.mattschlenkrich.paydaycalculator.model.WorkDates

private const val TAG = "WorkDateUpdate"

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
    private val df = DateFunctions()
//    private val cf = CommonFunctions()

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
        fillValues()
        setActions()
        setDateAction()
    }

    private fun setDateAction() {
        binding.apply {
            tvWorkDate.setOnClickListener {
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
    }

    private fun setActions() {
        binding.apply {
            fabDone.setOnClickListener {
                updateWorkDate()
            }
            fabAddExtra.setOnClickListener {
                addExtra()
            }
        }
    }

    private fun addExtra() {
        binding.apply {
            mainActivity.mainViewModel.setWorkDateObject(getCurWorkDate())

        }
    }

    private fun updateWorkDate() {
        binding.apply {
            mainActivity.payDayViewModel.updateWorkDate(
                getCurWorkDate()
            )
        }
        gotoTimeSheet()
    }

    private fun gotoTimeSheet() {
        mView.findNavController().navigate(
            WorkDateUpdateFragmentDirections
                .actionWorkDateUpdateFragmentToTimeSheetFragment()
        )
    }

    private fun fillValues() {
        if (mainActivity.mainViewModel.getWorkDateObject() != null) {
            curDate = mainActivity.mainViewModel.getWorkDateObject()!!
            curDateString = curDate.wdDate
            binding.apply {
                tvWorkDate.text = df.getDisplayDate(curDate.wdDate)
                etHours.setText(curDate.wdRegHours.toString())
                etOt.setText(curDate.wdOtHours.toString())
                etDblOt.setText(curDate.wdDblOtHours.toString())
                etStat.setText(curDate.wdStatHours.toString())
            }
            fillExtras()
        }
    }

    private fun getCurWorkDate(): WorkDates {
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

    fun fillExtras() {
        activity?.let {
            mainActivity.payDayViewModel.getWorkDateExtras(curDate.workDateId)
                .observe(viewLifecycleOwner) { extras ->
                    workDateExtras.clear()
                    extras.listIterator().forEach {
                        workDateExtras.add(it)
                    }
                }
        }
        binding.apply {
            val extraAdapter = WorkDateUpdateExtraAdapter(
                mainActivity, mView,
                this@WorkDateUpdateFragment,
                curDate,
                workDateExtras
            )
            rvExtras.apply {
                layoutManager = LinearLayoutManager(mView.context)
                adapter = extraAdapter
            }
            activity?.let {
                mainActivity.workExtraViewModel.getExtraDefinitionsPerDay(
                    mainActivity.mainViewModel.getWorkDateObject()!!.wdEmployerId
                ).observe(viewLifecycleOwner) { extras ->
                    extraAdapter.differ.submitList(extras)
                    Log.d(TAG, "extraAdapter size is ${extras.size}")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}