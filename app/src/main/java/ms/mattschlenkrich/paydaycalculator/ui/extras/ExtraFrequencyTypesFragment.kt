package ms.mattschlenkrich.paydaycalculator.ui.extras

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.adapter.WorkExtraFrequencyAdapter
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentExtraFrequencyTypesBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkExtraFrequencies

class ExtraFrequencyTypesFragment : Fragment() {

    private var _binding: FragmentExtraFrequencyTypesBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExtraFrequencyTypesBinding.inflate(inflater, container, false)
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.choose_extra_frequency_types)

        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateList()
        setActions()
    }

    private fun setActions() {
        binding.apply {
            fabNew.setOnClickListener {
                mView.findNavController().navigate(
                    ExtraFrequencyTypesFragmentDirections
                        .actionExtraFrequencyTypesFragmentToExtraFrequencyTypeAddFragment()
                )
            }
        }
    }

    private fun updateList() {
        val workExtraFrequencyAdapter = WorkExtraFrequencyAdapter(
            mainActivity, mView
        )
        binding.rvType.apply {
            layoutManager = StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
            setHasFixedSize(true)
            adapter = workExtraFrequencyAdapter
        }
        activity.let {
            mainActivity.workExtraViewModel.getWorkExtraFrequency().observe(
                viewLifecycleOwner
            ) { extraFrequencies ->
                workExtraFrequencyAdapter.differ.submitList(extraFrequencies)
                updateUI(extraFrequencies)
            }
        }
    }

    private fun updateUI(extraFrequencies: List<WorkExtraFrequencies>) {
        binding.apply {
            if (extraFrequencies.isEmpty()) {
                crdNoInfo.visibility = View.VISIBLE
                rvType.visibility = View.GONE
            } else {
                crdNoInfo.visibility = View.GONE
                rvType.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}