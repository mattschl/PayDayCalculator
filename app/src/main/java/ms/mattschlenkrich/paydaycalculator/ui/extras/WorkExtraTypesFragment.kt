package ms.mattschlenkrich.paydaycalculator.ui.extras

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.adapter.ExtraTypeAdapter
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkExtraTypesBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkExtraTypes


class WorkExtraTypesFragment : Fragment(
    R.layout.fragment_work_extra_types
) {

    private var _binding: FragmentWorkExtraTypesBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var extraTypeAdapter: ExtraTypeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkExtraTypesBinding.inflate(
            layoutInflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.view_extra_types)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fillExtraTypeList()
    }

    private fun fillExtraTypeList() {
        extraTypeAdapter = ExtraTypeAdapter(
            mainActivity, mView
        )
        binding.rvTypes.apply {
            layoutManager = StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
            setHasFixedSize(true)
            adapter = extraTypeAdapter
        }
        activity?.let {
//            mainActivity.workExtraViewModel.getExtraDefinitionTypes(
//            ).observe(
//                viewLifecycleOwner
//            ) { typesList ->
//                extraTypeAdapter.differ.submitList(typesList)
//                updateUI(typesList)
//            }
        }

    }

    private fun updateUI(typesList: List<WorkExtraTypes>) {
        binding.apply {
            if (typesList.isEmpty()) {
                crdNoInfo.visibility = View.VISIBLE
                rvTypes.visibility = View.GONE
            } else {
                crdNoInfo.visibility = View.GONE
                rvTypes.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}