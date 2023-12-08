package ms.mattschlenkrich.paydaycalculator.ui.tax

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentTaxTypeBinding

class TaxTypeFragment : Fragment(R.layout.fragment_tax_type) {

    private var _binding: FragmentTaxTypeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaxTypeBinding.inflate(inflater, container, false)
        mView = binding.root
        return mView
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}