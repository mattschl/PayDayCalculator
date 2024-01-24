package ms.mattschlenkrich.paydaycalculator.ui.paydays

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.adapter.WorkDateAdapter
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.WAIT_500
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentTimeSheetBinding
import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.model.PayPeriods
import ms.mattschlenkrich.paydaycalculator.payFunctions.PayCalculations
import ms.mattschlenkrich.paydaycalculator.payFunctions.PayDayProjections
import java.time.LocalDate

class TimeSheetFragment : Fragment(R.layout.fragment_time_sheet) {

    private var _binding: FragmentTimeSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private var curEmployer: Employers? = null
    private val cutOffs = ArrayList<String>()
    private var curCutOff = ""
    private val projections = PayDayProjections()
    private val df = DateFunctions()
    private lateinit var payCalculations: PayCalculations
    private var workDateAdapter: WorkDateAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimeSheetBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = resources.getString(R.string.time_sheet)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fillEmployers()
        selectEmployer()
        selectCutOffDate()
        setActions()
    }

    private fun setActions() {
        binding.apply {
            fabAddDate.setOnClickListener {
                addWorkDate()
            }
        }
    }

    private fun addWorkDate() {
        mainActivity.mainViewModel.setPayPeriod(getPayPeriod())
        mView.findNavController().navigate(
            TimeSheetFragmentDirections
                .actionTimeSheetFragmentToWorkDateAddFragment()
        )
    }

    private fun getPayPeriod(): PayPeriods {
        binding.apply {
            return PayPeriods(
                spCutOff.selectedItem.toString(),
                curEmployer!!.employerId,
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun selectCutOffDate() {
        binding.apply {
            spCutOff.onItemSelectedListener =
                object : OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (spCutOff.selectedItem.toString() !=
                            getString(R.string.generate_a_new_cut_off)
                        ) {
                            curCutOff = spCutOff.selectedItem.toString()
                            fillPayDayDetails()
                            fillWorkDates()
                        } else {
                            generateCutOff()
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
//                        fillCutOffDates()
                    }
                }
        }
    }

    private fun fillPayDayDetails() {
        if (curCutOff != "" && curEmployer != null) {
            payCalculations = PayCalculations(
                mainActivity,
                curEmployer!!,
                curCutOff,
                mView
            )
            binding.apply {
                var display = df.getDisplayDate(
                    LocalDate.parse(curCutOff)
                        .plusDays(curEmployer!!.cutoffDaysBefore.toLong()).toString()
                ) + " - Pay Summary"
                tvPaySummary.text = display
            }
        }
    }

    private fun fillWorkDates() {
        binding.apply {
            workDateAdapter = null
            workDateAdapter = WorkDateAdapter(
                mainActivity, mView
            )
            rvDates.apply {
                layoutManager = LinearLayoutManager(mView.context)
                adapter = workDateAdapter
            }
            activity?.let {
                mainActivity.payDayViewModel.getWorkDatesAndExtras(
                    curEmployer!!.employerId,
                    curCutOff
                ).observe(viewLifecycleOwner) { workDates ->
                    workDateAdapter!!.differ.submitList(workDates)
                    updateUI(workDates)
                }
            }
        }
    }

    private fun updateUI(workDates: List<Any>) {
        binding.apply {
            if (workDates.isEmpty()) {
                rvDates.visibility = View.GONE
            } else {
                rvDates.visibility = View.VISIBLE
            }
        }
    }

    private fun generateCutOff() {
        val nextCutOff = projections.generateNextCutOff(
            curEmployer!!,
            if (cutOffs.isEmpty()) "" else cutOffs[0]
        )
        mainActivity.payDayViewModel.insertPayPeriod(
            PayPeriods(
                nextCutOff,
                curEmployer!!.employerId,
                false,
                df.getCurrentTimeAsString()
            )
        )
        fillCutOffDates()
    }

    private fun selectEmployer() {
        binding.apply {
            spEmployers.onItemSelectedListener =
                object : OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (spEmployers.selectedItem.toString() !=
                            getString(R.string.add_new_employer)
                        ) {
                            mainActivity.employerViewModel.findEmployer(
                                spEmployers.selectedItem.toString()
                            ).observe(viewLifecycleOwner) { employer ->
                                curEmployer = employer
                            }
                            mainActivity.title = getString(R.string.time_sheet) +
                                    " for ${spEmployers.selectedItem}"
                            fillCutOffDates()
                        } else {
                            gotoEmployerAdd()
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        fillEmployers()
                    }
                }
        }
    }

    private fun gotoEmployerAdd() {
        mView.findNavController().navigate(
            TimeSheetFragmentDirections
                .actionTimeSheetFragmentToEmployerAddFragment()
        )
    }

    private fun fillCutOffDates() {
        if (curEmployer != null) {
            binding.apply {
                val cutOffAdapter = ArrayAdapter<Any>(
                    mView.context,
                    R.layout.spinner_item_bold
                )
                mainActivity.payDayViewModel.getCutOffDates(curEmployer!!.employerId).observe(
                    viewLifecycleOwner
                ) { dates ->
                    cutOffAdapter.clear()
                    cutOffs.clear()
                    cutOffAdapter.notifyDataSetChanged()
                    dates.listIterator().forEach {
                        cutOffAdapter.add(it.ppCutoffDate)
                        cutOffs.add(it.ppCutoffDate)
                    }
                    if (dates.isEmpty()) {
                        generateCutOff()
                    } else {
                        cutOffAdapter.add(getString(R.string.generate_a_new_cut_off))
                    }
                }
                spCutOff.adapter = cutOffAdapter
//                gotoCurrentCutoff()
            }

        }
    }

    private fun gotoCurrentCutoff() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_500)
            binding.apply {
                val dateNow = LocalDate.parse(df.getCurrentDateAsString())
                for (i in 0 until spCutOff.adapter.count - 1) {
                    val chkDate = LocalDate.parse(spCutOff.getItemAtPosition(i).toString())
                    if (chkDate <= dateNow) {
                        spCutOff.setSelection(i)
                        break
                    }
                }
            }
        }
    }

    private fun fillEmployers() {
        val employerAdapter = ArrayAdapter<String>(
            mView.context,
            R.layout.spinner_item_bold
        )
        mainActivity.employerViewModel.getEmployers().observe(
            viewLifecycleOwner
        ) { employers ->
            employerAdapter.clear()
            employerAdapter.notifyDataSetChanged()
            employers.listIterator().forEach {
                employerAdapter.add(it.employerName)
                curEmployer = employers[0]
            }
//            updateUI(employers)
            employerAdapter.add(getString(R.string.add_new_employer))
//            fillCutOffDates()
        }
        binding.spEmployers.adapter = employerAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}