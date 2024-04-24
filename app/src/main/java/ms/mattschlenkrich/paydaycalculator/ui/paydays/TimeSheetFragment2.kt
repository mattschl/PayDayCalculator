package ms.mattschlenkrich.paydaycalculator.ui.paydays

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.adapter.WorkDateAdapter
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.common.WAIT_100
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentTimeSheetBinding
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.model.payperiod.PayPeriods
import ms.mattschlenkrich.paydaycalculator.payFunctions.PayDayProjections
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import java.time.LocalDate

private const val TAG = "TimeSheet2"

class TimeSheetFragment2 : Fragment(R.layout.fragment_time_sheet), ITimeSheetFragment {
    private var _binding: FragmentTimeSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private var curEmployer: Employers? = null
    private val cutOffs = ArrayList<String>()
    private var curPayPeriod: PayPeriods? = null
    private var curCutOff = ""
    private val projections = PayDayProjections()
    private var workDateAdapter: WorkDateAdapter? = null
    private var valuesFilled = false
    private val nf = NumberFunctions()
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimeSheetBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fillEmployers()
        setActions()
        selectEmployer()
        selectCutOffDate()
    }

    private fun selectCutOffDate() {
        binding.apply {
            spCutOff.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (spCutOff.adapter.count > 0) {
                            if (spCutOff.selectedItem.toString() !=
                                getString(R.string.generate_a_new_cut_off)
                            ) {
                                curCutOff = spCutOff.selectedItem.toString()
                                if (valuesFilled) mainActivity.mainViewModel.setCutOffDate(curCutOff)
                                fillPayDayDate()
                                fillWorkDates()
                                fillValues()
                            } else {
                                generateCutOff()
                            }
                        } else {
                            generateCutOff()
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
//                        fillCutOffDates()*///-
                    }
                }
        }
    }

    private fun fillWorkDates() {
        binding.apply {
            workDateAdapter = null
            workDateAdapter = WorkDateAdapter(
                mainActivity, curCutOff, curEmployer!!, mView, this@TimeSheetFragment2
            )
            rvDates.apply {
                layoutManager = LinearLayoutManager(mView.context)
                adapter = workDateAdapter
            }
            activity?.let {
                mainActivity.payDayViewModel.getWorkDateList(
                    curEmployer!!.employerId,
                    curCutOff
                ).observe(viewLifecycleOwner) { workDates ->
                    workDateAdapter!!.differ.submitList(workDates)
                    updateUI(workDates)
                }
            }
        }
    }

    private fun fillPayDayDate() {
        if (curCutOff != "" && curEmployer != null) {
            binding.apply {
                val display = df.getDisplayDate(
                    LocalDate.parse(curCutOff)
                        .plusDays(curEmployer!!.cutoffDaysBefore.toLong()).toString()
                ) + " - Pay Summary"
                tvPaySummary.text = display
            }
        }
    }


    private fun selectEmployer() {
        binding.apply {
            spEmployers.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (spEmployers.selectedItem.toString() !=
                            getString(R.string.add_new_employer)
                        ) {
                            CoroutineScope(Dispatchers.IO).launch {
                                curEmployer = mainActivity.employerViewModel.findEmployer(
                                    spEmployers.selectedItem.toString()
                                )
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(WAIT_100)
                                if (valuesFilled) mainActivity.mainViewModel.setEmployer(curEmployer)
                                mainActivity.title = getString(R.string.pay_details) +
                                        " for ${spEmployers.selectedItem}"
                                fillCutOffDates()
                            }
                        } else {
                            gotoEmployerAdd()
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
//                        fillEmployers()
                    }
                }
        }
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
//                    Log.d(TAG, "cutOffAdapter has ${dates.size}")
                    if (dates.isEmpty() ||
                        dates[0].ppCutoffDate < df.getCurrentDateAsString()
                    ) {
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

    private fun generateCutOff() {
        val nextCutOff = projections.generateNextCutOff(
            curEmployer!!,
            if (cutOffs.isEmpty()) "" else cutOffs[0]
        )
        mainActivity.payDayViewModel.insertPayPeriod(
            PayPeriods(
                nf.generateId(),
                nextCutOff,
                curEmployer!!.employerId,
                false,
                df.getCurrentTimeAsString()
            )
        )
        fillCutOffDates()
    }

    private fun gotoEmployerAdd() {
        mainActivity.mainViewModel.setCallingFragment(TAG)
        mainActivity.mainViewModel.setEmployer(null)
        mainActivity.mainViewModel.setCutOffDate(null)
        mView.findNavController().navigate(
            TimeSheetFragmentDirections
                .actionTimeSheetFragmentToEmployerAddFragment()
        )
    }

    private fun setActions() {
        binding.apply {
            fabAddDate.setOnClickListener {
                addWorkDate()
            }
            crdPayDetails.setOnClickListener {
                gotoPayDetails()
            }
        }
    }

    private fun gotoPayDetails() {
        TODO("Not yet implemented")
    }

    private fun addWorkDate() {
        mainActivity.mainViewModel.setPayPeriod(getPayPeriod())
        mainActivity.mainViewModel.setCutOffDate(curCutOff)
        mainActivity.mainViewModel.setEmployer(curEmployer)
        mView.findNavController().navigate(
            TimeSheetFragmentDirections
                .actionTimeSheetFragmentToWorkDateAddFragment()
        )
    }

    private fun getPayPeriod(): PayPeriods {
        binding.apply {
            return PayPeriods(
                nf.generateId(),
                spCutOff.selectedItem.toString(),
                curEmployer!!.employerId,
                false,
                df.getCurrentTimeAsString()
            )
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
//                curEmployer = employers[0]
            }
            updateUI(employers)
            employerAdapter.add(getString(R.string.add_new_employer))
//            fillCutOffDates()
        }
        binding.spEmployers.adapter = employerAdapter
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


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun fillValues() {
        //TODO("Not yet implemented")
    }
}