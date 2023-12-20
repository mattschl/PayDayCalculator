package ms.mattschlenkrich.paydaycalculator.ui.tax

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.adapter.TaxRuleAdapter
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentTaxRulesBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkTaxRules

class TaxRulesFragment : Fragment(R.layout.fragment_tax_rules) {

    private var _binding: FragmentTaxRulesBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaxRulesBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = "View Tax Rules"
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActions()
        fillTaxTypes()
        fillEffectiveDates()
        fillTaxRuleList()
    }

    private fun fillEffectiveDates() {
        val dateAdapter = ArrayAdapter<Any>(
            mView.context,
            R.layout.spinner_item_bold
        )
    }

    private fun fillTaxTypes() {
        val taxTypeAdapter = ArrayAdapter<Any>(
            mView.context,
            R.layout.spinner_item_bold
        )
        mainActivity.workTaxViewModel.getTaxTypes().observe(
            viewLifecycleOwner
        ) { types ->
            taxTypeAdapter.clear()
            types.listIterator().forEach {
                taxTypeAdapter.add(it.workTaxType)
            }
        }
        binding.spTaxType.adapter = taxTypeAdapter
    }

    private fun fillTaxRuleList() {
        val taxRuleAdapter = TaxRuleAdapter(
            mainActivity, mView
        )
        binding.rvTaxRules.apply {
            layoutManager = StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
            setHasFixedSize(true)
            adapter = taxRuleAdapter
        }
        activity.let {
            mainActivity.workTaxViewModel.getTaxRules().observe(
                viewLifecycleOwner
            ) { taxRules ->
                taxRuleAdapter.differ.submitList(taxRules)
                updateUI(taxRules)
            }
        }
    }

    private fun updateUI(taxRules: List<WorkTaxRules>) {
        binding.apply {
            if (taxRules.isEmpty()) {
                rvTaxRules.visibility = View.GONE
                crdNoInfo.visibility = View.VISIBLE
            } else {
                rvTaxRules.visibility = View.VISIBLE
                crdNoInfo.visibility = View.GONE
            }
        }
    }

    private fun setActions() {
        binding.fabNew.setOnClickListener {
            mView.findNavController().navigate(
                TaxRulesFragmentDirections
                    .actionTaxRulesFragmentToTaxRuleAddFragment()
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}