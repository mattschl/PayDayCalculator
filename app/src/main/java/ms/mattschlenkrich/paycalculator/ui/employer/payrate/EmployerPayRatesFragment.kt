package ms.mattschlenkrich.paycalculator.ui.employer.payrate

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
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.FRAG_PAY_RATES
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.databinding.FragmentEmployerPayRatesBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.employer.adapter.EmployerWageAdapter

private const val TAG = FRAG_PAY_RATES

class EmployerPayRatesFragment :
    Fragment(R.layout.fragment_employer_pay_rates) {

    private var _binding: FragmentEmployerPayRatesBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var employerList: List<Employers>
    private var curEmployer: Employers? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployerPayRatesBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.view_or_edit_wages)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateEmployers()
        setClickActions()
    }

    private fun populateEmployers() {
        binding.apply {
            val employerAdapter = ArrayAdapter<String>(
                mView.context, R.layout.spinner_item_bold
            )
            mainActivity.employerViewModel.getEmployers().observe(
                viewLifecycleOwner
            ) { employers ->
                employerAdapter.clear()
                employerAdapter.notifyDataSetChanged()
                employerList = employers
                employers.listIterator().forEach {
                    employerAdapter.add(it.employerName)
                }
                employerAdapter.add(getString(R.string.add_new_employer))
                if (employerList.isNotEmpty()) {
                    curEmployer = employerList[0]
                }

            }
            spEmployers.adapter = employerAdapter
            getEmployerFromHistory()
        }
    }

    private fun getEmployerFromHistory() {
        binding.apply {
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_250)
                if (mainActivity.mainViewModel.getEmployer() != null) {
                    curEmployer = mainActivity.mainViewModel.getEmployer()
                    for (i in 0 until spEmployers.adapter.count) {
                        if (spEmployers.getItemAtPosition(i) == curEmployer!!.employerName) {
                            spEmployers.setSelection(i)
                            break
                        }
                    }
                }
            }
        }
    }

    private fun populatePayRates() {
        if (curEmployer != null) {
            binding.apply {
                val payRateAdapter = EmployerWageAdapter(
                    mainActivity, mView, this@EmployerPayRatesFragment, curEmployer!!, TAG
                )
                rvWage.apply {
                    layoutManager = LinearLayoutManager(
                        mView.context
                    )
                    adapter = payRateAdapter
                }
                activity.let {
                    mainActivity.employerViewModel.getEmployerPayRates(curEmployer!!.employerId)
                        .observe(viewLifecycleOwner) { payRates ->
                            payRateAdapter.differ.submitList(payRates)
                            updateUI(payRates)
                        }
                }
            }
        }
    }

    private fun setClickActions() {
        onSelectEmployer()
        binding.apply {
            fabNew.setOnClickListener {
                gotoPayRates()
            }
        }
    }

    private fun onSelectEmployer() {
        binding.apply {
            spEmployers.onItemSelectedListener =
                object : OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (spEmployers.selectedItem.toString() == getString(R.string.add_new_employer)) {
                            gotoAddEmployer()
                        } else {
                            curEmployer = employerList[spEmployers.selectedItemPosition]
                            populatePayRates()
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        //not needed
                    }
                }
        }
    }

    private fun updateUI(payRates: List<Any>) {
        binding.apply {
            if (payRates.isNotEmpty()) {
                rvWage.visibility = View.VISIBLE
                crdNoInfo.visibility = View.GONE
            } else {
                rvWage.visibility = View.GONE
                crdNoInfo.visibility = View.VISIBLE
            }
        }
    }

    private fun gotoAddEmployer() {
        mainActivity.mainViewModel.addCallingFragment(TAG)
        mainActivity.mainViewModel.setPayRate(null)
        gotoEmployerAddFragment()
    }

    private fun gotoPayRates() {
        mainActivity.mainViewModel.setEmployer(curEmployer)
        mainActivity.mainViewModel.setPayRate(null)
        mainActivity.mainViewModel.setCallingFragment(TAG)
        gotoEmployerPayRateAddFragment()
    }

    private fun gotoEmployerAddFragment() {
        mView.findNavController().navigate(
            EmployerPayRatesFragmentDirections
                .actionEmployerPayRatesFragmentToEmployerAddFragment()
        )
    }

    private fun gotoEmployerPayRateAddFragment() {
        mView.findNavController().navigate(
            EmployerPayRatesFragmentDirections
                .actionEmployerPayRatesFragmentToEmployerPayRateAddFragment()
        )
    }

    fun gotoEmployerWageUpdateFragment() {
        mView.findNavController().navigate(
            EmployerPayRatesFragmentDirections
                .actionEmployerPayRatesFragmentToEmployerWageUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}