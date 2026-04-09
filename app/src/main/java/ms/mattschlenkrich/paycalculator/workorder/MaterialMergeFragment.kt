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
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_MATERIAL_MERGE
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_100
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.Material
import ms.mattschlenkrich.paycalculator.data.MaterialAndChild
import ms.mattschlenkrich.paycalculator.data.MaterialMerged
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentEntityMergeBinding

private const val TAG = FRAG_MATERIAL_MERGE

class MaterialMergeFragment : Fragment(R.layout.fragment_entity_merge) {

    private var _binding: FragmentEntityMergeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private lateinit var materialList: List<Material>

    private lateinit var childList: List<MaterialAndChild>
    private lateinit var mParent: Material
    private lateinit var mChild: Material
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
        mainActivity.topMenuBar.title = getString(R.string.marge_material)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        populateUI()
        populateMaterialLists()
        populateFromCache()
    }

    private fun populateUI() {
        binding.apply {
            lblMaster.text = getString(R.string.master_material)
            lblChild.text = getString(R.string.child_material_to_merge)
        }
    }

    private fun populateMaterialLists() {
        workOrderViewModel.getMaterialsList().observe(viewLifecycleOwner) { list ->
            materialList = list
            val mDescriptions = list.map { it.mName }
            val wpDescriptionAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_normal, mDescriptions
            )
            val listAdapter = MaterialMergedAdapter(mainActivity, mView, this)
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
            if (getMaterialId() != null) {
                workOrderViewModel.getMaterial(getMaterialId()!!)
                    .observe(viewLifecycleOwner) { material ->
                        if (getMaterialIsParent()) {
                            chooseAsParent(material)
                        } else {
                            chooseAsChild(material)
                        }
                    }
            }
        }
    }

    private fun setClickActions() {
        binding.apply {
            acParent.setOnItemClickListener { _, _, _, _ ->
                chooseAsParent(materialList.first { it.mName == acParent.text.toString() })
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
                chooseAsChild(materialList.first { it.mName == acChild.text.toString() })
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
                        if (childList.any { it.materialParent.mName == acChild.text.toString() }) {
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
            .setTitle(getString(R.string.add_new_material))
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                mainScope.launch {
                    insertNewMaterial(newDescription)
                    delay(WAIT_250)
                    binding.apply {
                        workOrderViewModel.getMaterial(acParent.text.toString())
                            .observe(viewLifecycleOwner) { mat ->
                                mParent = mat
                            }
                        workOrderViewModel.getMaterial(acChild.text.toString())
                            .observe(viewLifecycleOwner) { mat ->
                                mChild = mat
                            }
                    }
                    populateMaterialLists()
                    chooseOptionsForMerge()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun insertNewMaterial(newDescription: String) {
        workOrderViewModel.insertMaterial(
            Material(
                nf.generateRandomIdAsLong(),
                newDescription,
                0.0,
                0.0,
                false,
                df.getCurrentTimeAsString()

            )
        )
    }

    private fun findDescription(mName: String): Boolean {
        try {
            return materialList.any { it.mName == mName }
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
//            Log.d(
//                TAG,
//                "merge and replace called wpParent is ${wpParent.wpDescription} -- wpChild is ${wpChild.wpDescription}"
//            )
            try {
                Log.d(
                    TAG,
                    "merge and replace called -- mParent is ${mParent.mName}, mChild is ${mChild.mName}"
                )
                workOrderViewModel.updateMaterialMerged(
                    mChild.materialId,
                    mParent.materialId,
                    df.getCurrentTimeAsString()
                )
                delay(WAIT_250)
            } catch (e: SQLiteConstraintException) {
                Log.d(TAG, e.message.toString())
            } finally {
                try {
                    workOrderViewModel.deleteMaterialMerged(
                        mChild.materialId,
                    )
                    workOrderViewModel.deleteMaterial(
                        mChild.materialId,
                        df.getCurrentTimeAsString()
                    )
                } catch (e: Exception) {
                    Log.d(TAG, e.message.toString())
                    workOrderViewModel.deleteMaterialMerged(
                        mChild.materialId,
                    )
                    workOrderViewModel.deleteMaterial(
                        mChild.materialId,
                        df.getCurrentTimeAsString()
                    )
                } finally {
                    populateChildren()
                    binding.apply {
                        acParent.setText(mParent.mName)
                        acChild.setText("")
                    }
                }
            }
            populateMaterialLists()
        }
    }

    private fun mergeAndKeep() {
        workOrderViewModel.insertMaterialMerged(
            MaterialMerged(
                nf.generateRandomIdAsLong(),
                mParent.materialId,
                mChild.materialId,
                false,
                df.getCurrentTimeAsString()
            )

        )
        populateChildren()
        binding.acChild.setText("")
    }

    fun chooseAsParent(material: Material) {
        mainScope.launch {
            binding.apply {
                mParent = material
                Log.d(TAG, "chooseToMergeAsParent called -- mParent is ${mParent.mName}")
                mainViewModel.apply {
                    setMaterial(mParent)
                    setMaterialId(mParent.materialId)
                    setMaterialIsParent(true)
                }
                acParent.setText(mParent.mName)
                delay(WAIT_100)
                populateChildren()
            }
        }
    }

    private fun populateChildren() {
        binding.apply {
            workOrderViewModel.getMaterialAndChildList(mParent.materialId)
                .observe(viewLifecycleOwner) { list ->
                    childList = list
                    if (list.isNotEmpty()) {
                        crdChildren.visibility = View.VISIBLE
                        val childAdapter =
                            MaterialChildrenAdapter(mView, this@MaterialMergeFragment)
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

    fun chooseAsChild(material: Material) {
        binding.apply {
            mChild = material
            acChild.setText(material.mName)
        }
    }

    private fun gotoCallingFragment() {
        gotoMaterialUpdateFragment()
    }

    fun gotoMaterialUpdateFragment() {
        mView.findNavController().navigate(
            MaterialMergeFragmentDirections.actionMaterialMergeFragmentToMaterialUpdateFragment()
        )
    }

    fun removeMaterialAsChild(materialAndChild: MaterialAndChild) {
        workOrderViewModel.deleteMaterialMerged(materialAndChild.materialMerged.materialMergeId)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}