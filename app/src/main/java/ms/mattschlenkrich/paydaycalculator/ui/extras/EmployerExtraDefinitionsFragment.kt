package ms.mattschlenkrich.paydaycalculator.ui.extras

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ms.mattschlenkrich.paydaycalculator.R

class EmployerExtraDefinitionsFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_employer_extra_definitions, container, false)
    }


}