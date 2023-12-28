package ms.mattschlenkrich.paydaycalculator.ui.extras

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentEmployerExtraDefinitionsBinding


class EmployerExtraDefinitionsFragment : Fragment(R.layout.fragment_employer_extra_definitions) {

    @Suppress("PropertyName")
    var _binding: FragmentEmployerExtraDefinitionsBinding? = null
    val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity

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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}