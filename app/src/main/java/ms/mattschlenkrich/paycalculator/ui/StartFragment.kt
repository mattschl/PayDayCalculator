package ms.mattschlenkrich.paycalculator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.NavGraphDirections
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.databinding.FragmentStartBinding

class StartFragment : Fragment(R.layout.fragment_start) {

    private var _binding: FragmentStartBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.app_name)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(1500)
            gotoTimeSheetFragment()
        }
    }

    private fun gotoTimeSheetFragment() {
        findNavController().navigate(
            NavGraphDirections.actionGlobalTimeSheetFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}