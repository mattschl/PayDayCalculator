package ms.mattschlenkrich.paydaycalculator.ui.extras

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentExtraFrequencyTypeUpdateBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkExtraFrequencies

class ExtraFrequencyTypeUpdateFragment : Fragment(R.layout.fragment_extra_frequency_type_update) {
    private var _binding: FragmentExtraFrequencyTypeUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
//    private val df = DateFunctions()

    //    private val cf = CommonFunctions()
    private val extraFrequencies = ArrayList<WorkExtraFrequencies>()
    private lateinit var curFrequencyType: WorkExtraFrequencies


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExtraFrequencyTypeUpdateBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = "Update Extra Frequency Type"
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getExtraFrequencyList()
        fillMenu()
        setActions()
        fillValues()
    }

    private fun setActions() {
        binding.apply {
            fabDone.setOnClickListener {
                updateExtraFrequencyType()
            }
        }
    }

    private fun updateExtraFrequencyType() {
        binding.apply {
            val message = checkExtraFrequency()
            if (message == ANSWER_OK) {
                mainActivity.workExtraViewModel.updateExtraFrequency(
                    WorkExtraFrequencies(
                        etFrequency.text.toString()
                    )
                )
                gotoCallingFragment()
            } else {
                Toast.makeText(
                    mView.context,
                    message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun checkExtraFrequency(): String {
        binding.apply {
            var nameFound = false
            if (extraFrequencies.isNotEmpty()) {
                for (frequency in extraFrequencies) {
                    if (frequency.workExtraFrequency == etFrequency.text.toString()) {
                        nameFound = true
                        break
                    }
                }
            }
            val errorMessage = if (etFrequency.text.isNullOrBlank()) {
                "    ERROR!!\n" +
                        "The extra needs frequency name"
            } else if (nameFound
                && etFrequency.text.toString() != curFrequencyType.workExtraFrequency
            ) {
                "    ERROR!!\n" +
                        "This extra frequency already exists!"
            } else {
                ANSWER_OK
            }
            return errorMessage
        }
    }

    private fun fillValues() {
        binding.apply {
            if (mainActivity.mainViewModel.getExtraFrequencyType() != null) {
                curFrequencyType = mainActivity.mainViewModel.getExtraFrequencyType()!!
                etFrequency.setText(curFrequencyType.workExtraFrequency)
            }
        }
    }

    private fun fillMenu() {
        mainActivity.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_delete, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_delete -> {
                        deleteEmployer()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun deleteEmployer() {
        mainActivity.workExtraViewModel.updateExtraFrequency(
            WorkExtraFrequencies(
                curFrequencyType.workExtraFrequency
            )
        )
        gotoCallingFragment()
    }

    private fun gotoCallingFragment() {
        mainActivity.mainViewModel.setExtraFrequencyType(null)
        mView.findNavController().navigate(
            ExtraFrequencyTypeUpdateFragmentDirections
                .actionExtraFrequencyTypeUpdateFragment2ToExtraFrequencyTypesFragment()
        )
    }

    private fun getExtraFrequencyList() {
        mainActivity.workExtraViewModel.getWorkExtraFrequency().observe(
            viewLifecycleOwner
        ) { list ->
            extraFrequencies.clear()
            list.listIterator().forEach {
                extraFrequencies.add(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}