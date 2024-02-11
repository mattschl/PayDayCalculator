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
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.FRAG_PAY_RATES
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentEmployerPayRatesBinding
import ms.mattschlenkrich.paydaycalculator.model.Employers

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
    ): View? {
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
        selectEmployer()
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
                        if (spEmployers.selectedItem.toString() == getString(R.string.add_new_employer)) {
                            gotoAddEmployer()
                        } else {
                            curEmployer = employerList[spEmployers.selectedItemPosition]
                            fillWages()
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        //not needed
                    }
                }
        }
    }

    private fun fillWages() {
        TODO("Not yet implemented")
    }

    private fun gotoAddEmployer() {
        mainActivity.mainViewModel.addCallingFragment(TAG)
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
                curEmployer = employerList[0]

            }
            spEmployers.adapter = employerAdapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}