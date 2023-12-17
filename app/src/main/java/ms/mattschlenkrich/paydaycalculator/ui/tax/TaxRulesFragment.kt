package ms.mattschlenkrich.paydaycalculator.ui.tax

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentTaxRulesBinding

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
        setTaxRuleList()
    }

    private fun setTaxRuleList() {
        TODO("Not yet implemented")
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