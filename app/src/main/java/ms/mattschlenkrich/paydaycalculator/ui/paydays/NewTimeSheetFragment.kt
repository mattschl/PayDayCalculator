package ms.mattschlenkrich.paydaycalculator.ui.paydays

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.adapter.WorkDateAdapter
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentTimeSheetBinding
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.payFunctions.PayDateProjections
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class NewTimeSheetFragment : Fragment(R.layout.fragment_time_sheet), ITimeSheetFragment {

    private var _binding: FragmentTimeSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private var curEmployer: Employers? = null
    private val cutOffs = ArrayList<String>()
    private var curCutOff = ""
    private val projections = PayDateProjections()
    private val nf = NumberFunctions()
    private val df = DateFunctions()
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
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fillEmployers()
        selectEmployer()
        selectCutOffDate()
        setActions()
        gotoEmployerAndPayDayFromHistory()
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
        TODO("Not yet implemented")
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
                        if (spCutOff.selectedItem.toString() !=
                            getString(R.string.generate_a_new_cut_off)
                        ) {
                            curCutOff = spCutOff.selectedItem.toString()
                            fillPayDayDate()
                            fillWorkDates()
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
        TODO("Not yet implemented")
    }

    private fun generateCutOff() {
        TODO("Not yet implemented")
    }

    private fun fillPayDayDate() {
        TODO("Not yet implemented")
    }

    private fun gotoEmployerAndPayDayFromHistory() {
        TODO("Not yet implemented")
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
                            mainActivity.employerViewModel.findEmployer(
                                spEmployers.selectedItem.toString()
                            ).observe(viewLifecycleOwner) { employer ->
                                curEmployer = employer
                                mainActivity.mainViewModel.setEmployerString(curEmployer!!.employerName)
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
        TODO("Not yet implemented")
    }

    private fun fillCutOffDates() {
        TODO("Not yet implemented")
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

    override fun fillValues() {
        TODO("Not yet implemented")
    }
}