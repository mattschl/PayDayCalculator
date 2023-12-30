package ms.mattschlenkrich.paydaycalculator.ui.extras

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.adapter.EmployerExtraDefinitionAdapter
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentEmployerExtraDefinitionsBinding
import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.model.ExtraDefinitionFull


class EmployerExtraDefinitionsFragment : Fragment(R.layout.fragment_employer_extra_definitions) {

    private var _binding: FragmentEmployerExtraDefinitionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val employerList = ArrayList<Employers>()
    private var curEmployer: Employers? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployerExtraDefinitionsBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = "View Extra income or deductions"
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActions()
        fillEmployers()
        selectEmployer()
    }

    private fun selectEmployer() {
        binding.apply {
            spEmployers.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    for (employer in employerList) {
                        if (employer.employerName == spEmployers.selectedItem.toString()) {
                            curEmployer = employer
                            break
                        }
                    }
                    fillExtrasList()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    //not needed
                }
            }
        }
    }

    private fun fillExtrasList() {
        binding.apply {
            val employerExtraDefinitionAdapter = EmployerExtraDefinitionAdapter(
                mainActivity, mView
            )
            rvExtras.apply {
                layoutManager = StaggeredGridLayoutManager(
                    2,
                    StaggeredGridLayoutManager.VERTICAL
                )
                setHasFixedSize(true)
                adapter = employerExtraDefinitionAdapter
            }
            activity.let {
                mainActivity.workExtraViewModel.getActiveExtraDefinitionsFull().observe(
                    viewLifecycleOwner
                ) { extras ->
                    employerExtraDefinitionAdapter.differ.submitList(extras)
                    updateRecycler(extras)
                }
            }
        }
    }

    private fun setActions() {
        binding.fabNew.setOnClickListener {
            gotoExtraAdd()
        }
    }

    private fun gotoExtraAdd() {
        mainActivity.mainViewModel.setEmployer(curEmployer)
        mView.findNavController().navigate(
            EmployerExtraDefinitionsFragmentDirections
                .actionEmployerExtraDefinitionsFragmentToEmployerExtraDefinitionsAddFragment()
        )
    }

    private fun fillEmployers() {
        val employerAdapter = ArrayAdapter<Any>(
            mView.context,
            R.layout.spinner_item_bold
        )
        mainActivity.employerViewModel.getEmployers().observe(
            viewLifecycleOwner
        ) { employers ->
            employerAdapter.clear()
            employerList.clear()
            employers.listIterator().forEach {
                employerAdapter.add(it.employerName)
                employerList.add(it)
            }
            updateUI(employers)
            if (employerAdapter.isEmpty) {
                employerAdapter.add(getString(R.string.no_employers_add_an_employer_through_the_employer_tab))
            }
        }
        binding.spEmployers.adapter = employerAdapter
    }

    private fun updateUI(employers: List<Employers>) {
        binding.apply {
            if (spEmployers.getItemAtPosition(0).toString() ==
                getString(R.string.no_employers_add_an_employer_through_the_employer_tab) ||
                employers.isEmpty()
            ) {
                fabNew.visibility = View.GONE
            } else {
                fabNew.visibility = View.VISIBLE
            }
        }
    }

    private fun updateRecycler(extras: List<ExtraDefinitionFull>) {
        binding.apply {
            if (extras.isEmpty()) {
                rvExtras.visibility = View.GONE
                crdNoInfo.visibility = View.VISIBLE
            } else {
                rvExtras.visibility = View.VISIBLE
                crdNoInfo.visibility = View.GONE
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}