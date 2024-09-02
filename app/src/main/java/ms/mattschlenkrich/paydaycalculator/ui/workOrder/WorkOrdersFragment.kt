package ms.mattschlenkrich.paydaycalculator.ui.workOrder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkOrdersBinding
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity


class WorkOrdersFragment :
    Fragment(R.layout.fragment_work_orders) {

    var _binding: FragmentWorkOrdersBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private var curEmployer: Employers? = null
    private var employerList = ArrayList<Employers>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkOrdersBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateEmployers()
        onSelectEmployer()
        setClickActions()
    }

    private fun populateEmployers() {
        TODO("Not yet implemented")
    }

    private fun onSelectEmployer() {
        TODO("Not yet implemented")
    }

    private fun setClickActions() {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}