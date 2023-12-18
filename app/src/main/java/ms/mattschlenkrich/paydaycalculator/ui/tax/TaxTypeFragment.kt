package ms.mattschlenkrich.paydaycalculator.ui.tax

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.adapter.TaxTypeAdapter
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentTaxTypeBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkTaxTypes

class TaxTypeFragment : Fragment(R.layout.fragment_tax_type) {

    private var _binding: FragmentTaxTypeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaxTypeBinding.inflate(inflater, container, false)
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.choose_a_tax_type)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fillTaxTypeList()
        setActions()
    }

    private fun setActions() {
        binding.apply {
            fabNew.setOnClickListener {
                mView.findNavController().navigate(
                    TaxTypeFragmentDirections
                        .actionTaxTypeFragmentToTaxTypeAddFragment()
                )
            }
        }
    }

    private fun fillTaxTypeList() {
        val taxTypeAdapter = TaxTypeAdapter(
            mainActivity, mView
        )
        binding.rvTaxTypes.apply {
            layoutManager = StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
            setHasFixedSize(true)
            adapter = taxTypeAdapter
        }
        activity.let {
            mainActivity.workTaxViewModel.getTaxTypes().observe(
                viewLifecycleOwner
            ) { taxTypes ->
                taxTypeAdapter.differ.submitList(taxTypes)
                updateUI(taxTypes)
            }
        }
    }

    private fun updateUI(taxTypes: List<WorkTaxTypes>) {
        binding.apply {
            if (taxTypes.isEmpty()) {
                crdNoInfo.visibility = View.VISIBLE
                rvTaxTypes.visibility = View.GONE
            } else {
                crdNoInfo.visibility = View.GONE
                rvTaxTypes.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}