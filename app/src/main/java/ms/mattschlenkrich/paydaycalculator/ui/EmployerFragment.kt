package ms.mattschlenkrich.paydaycalculator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentEmployerBinding
import ms.mattschlenkrich.paydaycalculator.viewModel.EmployerViewModel

class EmployerFragment : Fragment(R.layout.fragment_employer) {

    private var _binding: FragmentEmployerBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var employerViewModel: EmployerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployerBinding.inflate(layoutInflater)
        mView = binding.root
        mainActivity = (activity as MainActivity)
        employerViewModel = mainActivity.employerViewModel
        return mView
    }


}