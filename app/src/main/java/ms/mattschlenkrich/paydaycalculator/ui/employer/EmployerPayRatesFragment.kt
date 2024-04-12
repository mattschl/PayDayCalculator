package ms.mattschlenkrich.paydaycalculator.ui.employer

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
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.adapter.EmployerWageAdapter
import ms.mattschlenkrich.paydaycalculator.common.FRAG_PAY_RATES
import ms.mattschlenkrich.paydaycalculator.common.WAIT_250
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentEmployerPayRatesBinding
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

private const val TAG = FRAG_PAY_RATES

class EmployerPayRatesFragment :
    Fragment(R.layout.fragment_employer_pay_rates) {

    private var _binding: FragmentEmployerPayRatesBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val employerList = ArrayList<Employers>()
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
        fillEmployers()
        setActions()
        onSelectEmployer()
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
                            fillPayRates()
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        //not needed
                    }
                }
        }
    }

    private fun fillPayRates() {
        if (curEmployer != null) {
            binding.apply {
                val payRateAdapter = EmployerWageAdapter(
                    mainActivity, mView, curEmployer!!, TAG
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
        mView.findNavController().navigate(
            EmployerPayRatesFragmentDirections
                .actionEmployerPayRatesFragmentToEmployerAddFragment()
        )
    }

    private fun setActions() {
        binding.apply {
            fabNew.setOnClickListener {
                gotoWageAdd()
            }
        }
    }

    private fun gotoWageAdd() {
        mainActivity.mainViewModel.setEmployer(curEmployer)
        mainActivity.mainViewModel.setPayRate(null)
        mainActivity.mainViewModel.setCallingFragment(TAG)
        mView.findNavController().navigate(
            EmployerPayRatesFragmentDirections
                .actionEmployerPayRatesFragmentToEmployerPayRateAddFragment()
        )
    }

    private fun fillEmployers() {
        binding.apply {
            val employerAdapter = ArrayAdapter<String>(
                mView.context, R.layout.spinner_item_bold
            )
            mainActivity.employerViewModel.getEmployers().observe(
                viewLifecycleOwner
            ) { employers ->
                employerAdapter.clear()
                employerAdapter.notifyDataSetChanged()
                employerList.clear()
                employers.listIterator().forEach {
                    employerList.add(it)
                    employerAdapter.add(it.employerName)
                }
                employerAdapter.add(getString(R.string.add_new_employer))
                if (employerList.isNotEmpty()) {
                    curEmployer = employerList[0]
                }

            }
            spEmployers.adapter = employerAdapter
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}