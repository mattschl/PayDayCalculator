package ms.mattschlenkrich.paycalculator.workorder

import android.app.AlertDialog
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_PERFORMED_MERGE
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_100
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.common.WAIT_500
import ms.mattschlenkrich.paycalculator.data.WorkPerformed
import ms.mattschlenkrich.paycalculator.data.WorkPerformedAndChild
import ms.mattschlenkrich.paycalculator.data.WorkPerformedMerged
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentEntityMergeBinding
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.workorder.WorkPerformedChildrenAdapter
import ms.mattschlenkrich.paycalculator.workorder.WorkPerformedMergedAdapter

private const val TAG = FRAG_WORK_PERFORMED_MERGE

class WorkPerformedMergeFragment : Fragment(R.layout.fragment_entity_merge) {

    private var _binding: FragmentEntityMergeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private lateinit var workPerformedList: List<WorkPerformed>

    private lateinit var childList: List<WorkPerformedAndChild>
    private lateinit var wpParent: WorkPerformed
    private lateinit var wpChild: WorkPerformed
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val df = DateFunctions()
    private val nf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEntityMergeBinding.inflate(inflater, container, false)
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel
        mainActivity.topMenuBar.title = getString(R.string.merge_work_performed)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        populateWorkPerformedLists()
        populateFromCache()
    }

    private fun populateWorkPerformedLists() {
        workOrderViewModel.getWorkPerformedAll().observe(viewLifecycleOwner) { list ->
            workPerformedList = list
            val wpDescriptions = list.map { it.wpDescription }
            val wpDescriptionAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_normal, wpDescriptions
            )
            val listAdapter = WorkPerformedMergedAdapter(mainActivity, mView, this)
            listAdapter.differ.submitList(list)
            binding.apply {
                acParent.setAdapter(wpDescriptionAdapter)
                acChild.setAdapter(wpDescriptionAdapter)
                rvMergedList.layoutManager = LinearLayoutManager(mView.context)
                rvMergedList.adapter = listAdapter
            }
        }
    }

    private fun populateFromCache() {
        mainViewModel.apply {
            if (getWorkPerformedId() != null) {
                workOrderViewModel.getWorkPerformed(getWorkPerformedId()!!)
                    .observe(viewLifecycleOwner) { workPerformed ->
                        if (getWorkPerformedIsMaster()) {
                            chooseAsParent(workPerformed)
                        } else {
                            chooseAsChild(workPerformed)
                        }
                    }
            }
        }
    }

    private fun setClickActions() {
        binding.apply {
            acParent.setOnItemClickListener { _, _, _, _ ->
                chooseAsParent(workPerformedList.first { it.wpDescription == acParent.text.toString() })
            }
            acParent.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
//                    null
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                    setCurWorkOrder()
                }

                override fun afterTextChanged(s: Editable?) {
                    if (findDescription(acParent.text.toString())) {
                        populateChildren()
                    } else {
                        crdChildren.visibility = View.GONE
                    }
                }
            })
            acChild.setOnItemClickListener { _, _, _, _ ->
                chooseAsChild(workPerformedList.first { it.wpDescription == acChild.text.toString() })
            }
            acChild.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
//                    null
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                    setCurWorkOrder()
                }

                override fun afterTextChanged(s: Editable?) {
                    if (findDescription(acChild.text.toString())) {
                        if (childList.any { it.workPerformedChild.wpDescription == acChild.text.toString() }) {
                            acChild.error =
                                getString(R.string.this_work_performed_child_already_exists)
                        }
                    }
                }
            })
            btnMerge.setOnClickListener { validateDescriptions() }
//            btnMerge.setOnClickListener { chooseOptionsForMerge() }
            btnDone.setOnClickListener { gotoCallingFragment() }

        }
    }

    private fun validateDescriptions() {
        binding.apply {
            if (acParent.text.toString().isEmpty()) {
                acParent.error = getString(R.string.this_is_required)
                acParent.requestFocus()
            } else if (acChild.text.toString().isEmpty()) {
                acChild.error = getString(R.string.this_is_required)
                acChild.requestFocus()
            } else {
                if (!findDescription(acParent.text.toString())) {
                    chooseToAddDescriptionAndMerge(acParent.text.toString())
                } else if (!findDescription(acChild.text.toString())) {
                    chooseToAddDescriptionAndMerge(acChild.text.toString())
                } else {
                    chooseOptionsForMerge()
                }
            }
        }
    }

    private fun chooseToAddDescriptionAndMerge(newDescription: String) {
        AlertDialog.Builder(mView.context)
            .setTitle(getString(R.string.add_new_work_performed))
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                mainScope.launch {
                    insertNewWorkPerformed(newDescription)
                    delay(WAIT_250)
                    binding.apply {
                        workOrderViewModel.getWorkPerformed(acParent.text.toString())
                            .observe(viewLifecycleOwner) { wp ->
                                wpParent = wp
                            }
                        workOrderViewModel.getWorkPerformed(acChild.text.toString())
                            .observe(viewLifecycleOwner) { wp ->
                                wpChild = wp
                            }
                    }
                    populateWorkPerformedLists()
                    chooseOptionsForMerge()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun insertNewWorkPerformed(newDescription: String) {
        workOrderViewModel.insertWorkPerformed(
            WorkPerformed(
                nf.generateRandomIdAsLong(),
                newDescription,
                false,
                df.getCurrentTimeAsString()
            )
        )
    }

    private fun findDescription(wpDescription: String): Boolean {
        try {
            return workPerformedList.any { it.wpDescription == wpDescription }
        } catch (e: UninitializedPropertyAccessException) {
            Log.d(TAG, "exception is ${e.toString()}")
            return false
        }
    }

    private fun chooseOptionsForMerge() {
        AlertDialog.Builder(mView.context)
            .setTitle(getString(R.string.choose_merge_option))
            .setMessage(getString(R.string.either_merge_and_replace_or_merge_))
            .setPositiveButton(getString(R.string.merge_and_replace)) { _, _ ->
                mergeAndReplace()
            }
            .setNeutralButton(getString(R.string.merge_and_keep)) { _, _ ->
                mergeAndKeep()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun mergeAndReplace() {
        mainScope.launch {
            Log.d(
                TAG,
                "merge and replace called wpParent is ${wpParent.wpDescription} -- wpChild is ${wpChild.wpDescription}"
            )
            try {
                workOrderViewModel.updateWorkPerformedMerged(
                    wpChild.workPerformedId,
                    wpParent.workPerformedId
                )
                delay(WAIT_100)
            } catch (e: SQLiteConstraintException) {
                Log.d(TAG, e.message.toString())
            } finally {
                try {
                    workOrderViewModel.deleteWorkPerformedMerged(
                        wpChild.workPerformedId,
                    )
                    workOrderViewModel.deleteWorkPerformed(wpChild)
                } catch (e: Exception) {
                    Log.d(TAG, e.message.toString())
                    delay(WAIT_500)
                    workOrderViewModel.deleteWorkPerformedMerged(
                        wpChild.workPerformedId,
                    )
                    workOrderViewModel.deleteWorkPerformed(wpChild)
                } finally {
                    populateChildren()
                    binding.apply {
                        acParent.setText(wpParent.wpDescription)
                        acChild.setText("")
                    }
                }
            }
            populateWorkPerformedLists()
        }
    }

    private fun mergeAndKeep() {
        workOrderViewModel.insertWorkPerformedMerged(
            WorkPerformedMerged(
                nf.generateRandomIdAsLong(),
                wpParent.workPerformedId,
                wpChild.workPerformedId,
                false,
                df.getCurrentTimeAsString()
            )
        )
        populateChildren()
        binding.acChild.setText("")
    }

    fun chooseAsParent(workPerformed: WorkPerformed) {
        binding.apply {
            wpParent = workPerformed
            Log.d(TAG, "chooseToMergeAsParent called -- wpParent is ${workPerformed.wpDescription}")
            mainViewModel.apply {
                setWorkPerformedId(wpParent.workPerformedId)
                setWorkPerformedIsMaster(true)
            }
            acParent.setText(wpParent.wpDescription)
            populateChildren()
        }
    }

    private fun populateChildren() {
        binding.apply {
            workOrderViewModel.getWorkPerformedAndChildList(wpParent.workPerformedId)
                .observe(viewLifecycleOwner) { list ->
                    childList = list
                    if (list.isNotEmpty()) {
                        crdChildren.visibility = View.VISIBLE
                        val childAdapter =
                            WorkPerformedChildrenAdapter(mView, this@WorkPerformedMergeFragment)
                        childAdapter.differ.submitList(list)
                        rvChildren.apply {
                            layoutManager = LinearLayoutManager(mView.context)
                            adapter = childAdapter
                        }
                    } else {
                        crdChildren.visibility = View.GONE
                    }
                }
        }
    }

    fun chooseAsChild(workPerformed: WorkPerformed) {
        binding.apply {
            wpChild = workPerformed
            acChild.setText(workPerformed.wpDescription)
        }
    }

    private fun gotoCallingFragment() {
        gotoWorkPerformedUpdateFragment()
    }

    fun gotoWorkPerformedUpdateFragment() {
        mView.findNavController().navigate(
            WorkPerformedMergeFragmentDirections.actionWorkPerformedMergeFragmentToWorkPerformedUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun removeWorkPerformedChild(workPerformedAndChild: WorkPerformedAndChild) {
        workOrderViewModel.deleteWorkPerformedMerged(workPerformedAndChild.workPerformedMerged.workPerformedMergeId)

    }
}