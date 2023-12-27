package ms.mattschlenkrich.paydaycalculator.ui.employer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.adapter.EmployerAdapter
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentEmployerBinding
import ms.mattschlenkrich.paydaycalculator.model.Employers

class EmployerFragment : Fragment(R.layout.fragment_employer) {

    private var _binding: FragmentEmployerBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployerBinding.inflate(inflater, container, false)
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.view_employers)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fillEmployers()
        setActions()
    }

    private fun setActions() {
        binding.apply {
            fabNew.setOnClickListener {
                mView.findNavController().navigate(
                    EmployerFragmentDirections
                        .actionEmployerFragmentToEmployerAddFragment()
                )
            }
        }
    }

    private fun fillEmployers() {
        val employerAdapter = EmployerAdapter(
            mainActivity, mView
        )
        binding.rvEmployers.apply {
            layoutManager = StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
            setHasFixedSize(true)
            adapter = employerAdapter
        }
        activity?.let {
            mainActivity.employerViewModel.getEmployers().observe(
                viewLifecycleOwner
            ) { employer ->
                employerAdapter.differ.submitList(employer)
                updateUI(employer)
            }
        }
    }

    private fun updateUI(employer: List<Employers>?) {
        binding.apply {
            if (employer!!.isEmpty()) {
                rvEmployers.visibility = View.GONE
                crdNoInfo.visibility = View.VISIBLE
            } else {
                rvEmployers.visibility = View.VISIBLE
                crdNoInfo.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}