package ms.mattschlenkrich.paydaycalculator.ui.extras

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.CommonFunctions
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentExtraFrequencyTypeAddBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkExtraFrequencies

class ExtraFrequencyTypeAddFragment : Fragment(R.layout.fragment_extra_frequency_type_add) {

    private var _binding: FragmentExtraFrequencyTypeAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()
    private val cf = CommonFunctions()
    private val extraFrequencies = ArrayList<WorkExtraFrequencies>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExtraFrequencyTypeAddBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.add_a_new_extra_frequency)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getExtraFrequencyList()
        fillMenu()
    }

    private fun fillMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.save_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_save -> {
                        saveExtraFrequency()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun saveExtraFrequency() {
        binding.apply {
            val message = checkExtraFrequency()
            if (message == ANSWER_OK) {
                mainActivity.workExtraViewModel.insertExtraFrequency(
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

    private fun gotoCallingFragment() {
        mView.findNavController().navigate(
            ExtraFrequencyTypeAddFragmentDirections
                .actionExtraFrequencyTypeAddFragmentToExtraFrequencyTypesFragment()
        )
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
            } else if (nameFound) {
                "    ERROR!!\n" +
                        "This extra frequency already exists!"
            } else {
                ANSWER_OK
            }
            return errorMessage
        }
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