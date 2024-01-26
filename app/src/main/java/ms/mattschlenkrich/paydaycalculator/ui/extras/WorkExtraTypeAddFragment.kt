package ms.mattschlenkrich.paydaycalculator.ui.extras

import android.app.AlertDialog
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
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkExtraTypeAddBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkExtraTypes

class WorkExtraTypeAddFragment : Fragment(
    R.layout.fragment_work_extra_type_add
) {

    var _binding: FragmentWorkExtraTypeAddBinding? = null
    val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()
    private val cf = CommonFunctions()
    private val extraTypeList = ArrayList<WorkExtraTypes>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWorkExtraTypeAddBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = "Add a new Extra Type"
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getExtraTypeList()
        fillMenu()
    }

    private fun getExtraTypeList() {
        mainActivity.workExtraViewModel.getExtraDefinitionTypes().observe(
            viewLifecycleOwner
        ) { names ->
            extraTypeList.clear()
            names.listIterator().forEach {
                extraTypeList.add(it)
            }
        }
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
                        saveExtraType()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun saveExtraType() {
        binding.apply {
            val message = checkExtraType()
            if (message == ANSWER_OK) {
//                val extraType = WorkExtraTypes(
//                    cf.generateId(),
//                    etExtraName.text.toString().trim(),
//                    false,
//                    df.getCurrentTimeAsString()
//                )
//                mainActivity.workExtraViewModel.insertWorkExtraType(
//                    extraType
//                )
//                gotoNextStep(extraType)
            } else {
                Toast.makeText(
                    mView.context,
                    message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun gotoNextStep(extraType: WorkExtraTypes) {
        AlertDialog.Builder(mView.context)
            .setTitle("Choose the next step for ${extraType.wetName}")
            .setMessage(
                "The extra type has been added. " +
                        "Would you like to add another one or go back?"
            )
            .setPositiveButton("Add another") { _, _ ->
                binding.etExtraName.setText("")
            }
            .setNegativeButton("Go back") { _, _ ->
                gotoCallingFragment()
            }
    }

    private fun gotoCallingFragment() {
        mView.findNavController().navigate(
            WorkExtraTypeAddFragmentDirections
                .actionWorkExtraTypeAddFragmentToEmployerFragment()
        )
    }

    private fun checkExtraType(): String {
        binding.apply {
            var nameFound = false
            if (extraTypeList.isNotEmpty()) {
                for (extra in extraTypeList) {
                    if (extra.wetName == etExtraName.text.toString().trim()) {
                        nameFound = true
                        break
                    }
                }
            }
            val errorMessage = if (etExtraName.text.isNullOrBlank()) {
                "    ERROR!!\n" +
                        "THe Extra type must have a name"
            } else if (nameFound) {
                "   ERROR!!\n" +
                        "This Extra Type already exists"
            } else {
                ANSWER_OK
            }
            return errorMessage
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}