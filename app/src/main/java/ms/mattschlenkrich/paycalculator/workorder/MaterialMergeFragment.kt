package ms.mattschlenkrich.paycalculator.workorder

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_100
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.Material
import ms.mattschlenkrich.paycalculator.data.MaterialAndChild
import ms.mattschlenkrich.paycalculator.data.MaterialMerged
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel

class MaterialMergeFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel

    private val df = DateFunctions()
    private val nf = NumberFunctions()

    private var mParent by mutableStateOf<Material?>(null)
    private var mChild by mutableStateOf<Material?>(null)
    private var parentDescription by mutableStateOf("")
    private var childDescription by mutableStateOf("")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel

        return ComposeView(requireContext()).apply {
            setContent {
                val materialList by workOrderViewModel.getMaterialsList()
                    .observeAsState(emptyList())

                val childList by if (mParent != null) {
                    workOrderViewModel.getMaterialAndChildList(mParent!!.materialId)
                        .observeAsState(emptyList())
                } else {
                    remember { mutableStateOf(emptyList()) }
                }

                // Initial selection from cache
                LaunchedEffect(Unit) {
                    val cachedId = mainViewModel.getMaterialId()
                    if (cachedId != null) {
                        val m = withContext(Dispatchers.IO) {
                            workOrderViewModel.getMaterialSync(cachedId)
                        }
                        if (m != null) {
                            if (mainViewModel.getMaterialIsParent()) {
                                chooseAsParent(m)
                            } else {
                                chooseAsChild(m)
                            }
                        }
                    }
                }

                MaterialMergeScreen(
                    materialList = materialList,
                    parentDescription = parentDescription,
                    onParentDescriptionChange = { description ->
                        parentDescription = description
                        mParent = materialList.find { it.mName == description }
                    },
                    onParentSelected = {
                        chooseAsParent(it)
                    },
                    childList = childList,
                    onRemoveChild = {
                        workOrderViewModel.deleteMaterialMerged(it.materialMerged.materialMergeId)
                    },
                    childDescription = childDescription,
                    onChildDescriptionChange = { description ->
                        childDescription = description
                        mChild = materialList.find { it.mName == description }
                    },
                    onChildSelected = {
                        chooseAsChild(it)
                    },
                    onMergeClick = {
                        if (parentDescription.isNotBlank()) {
                            lifecycleScope.launch {
                                val existingParent = withContext(Dispatchers.IO) {
                                    workOrderViewModel.getMaterialSync(parentDescription)
                                }
                                if (existingParent == null) {
                                    chooseToAddDescriptionAndMerge(parentDescription)
                                } else {
                                    mParent = existingParent
                                    val currentChildDesc = childDescription
                                    if (currentChildDesc.isNotBlank()) {
                                        val existingChild = withContext(Dispatchers.IO) {
                                            workOrderViewModel.getMaterialSync(currentChildDesc)
                                        }
                                        if (existingChild == null) {
                                            chooseToAddChildAndMerge(currentChildDesc)
                                        } else if (existingChild.materialId != existingParent.materialId) {
                                            mChild = existingChild
                                            chooseOptionsForMerge(existingParent, existingChild) {
                                                mChild = null
                                                childDescription = ""
                                            }
                                        }
                                    } else if (mChild != null && mChild!!.materialId != existingParent.materialId) {
                                        chooseOptionsForMerge(existingParent, mChild!!) {
                                            mChild = null
                                            childDescription = ""
                                        }
                                    }
                                }
                            }
                        }
                    },
                    onDoneClick = {
                        findNavController().navigate(
                            MaterialMergeFragmentDirections.actionMaterialMergeFragmentToMaterialUpdateFragment()
                        )
                    },
                    onListItemSelected = {
                        if (mParent == null) {
                            chooseAsParent(it)
                        } else {
                            chooseAsChild(it)
                        }
                    }
                )
            }
        }
    }

    fun chooseAsParent(material: Material) {
        mParent = material
        parentDescription = material.mName
        mainViewModel.setMaterialId(material.materialId)
        mainViewModel.setMaterialIsParent(true)
    }

    fun chooseAsChild(material: Material) {
        mChild = material
        childDescription = material.mName
    }

    fun removeMaterialAsChild(materialAndChild: MaterialAndChild) {
        workOrderViewModel.deleteMaterialMerged(materialAndChild.materialMerged.materialMergeId)
    }

    private fun chooseToAddDescriptionAndMerge(newName: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.add_new_material))
            .setMessage(
                getString(
                    R.string.the_material_does_not_exist_would_you_like_to_add_it,
                    newName
                )
            )
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                lifecycleScope.launch {
                    val existing = withContext(Dispatchers.IO) {
                        workOrderViewModel.getMaterialSync(newName)
                    }
                    if (existing == null) {
                        val newMaterial = Material(
                            nf.generateRandomIdAsLong(),
                            newName,
                            0.0,
                            0.0,
                            false,
                            df.getCurrentTimeAsString()
                        )
                        workOrderViewModel.insertMaterial(newMaterial).join()
                        val newlyCreated = withContext(Dispatchers.IO) {
                            workOrderViewModel.getMaterialSync(newName)
                        }
                        if (newlyCreated != null) {
                            chooseAsParent(newlyCreated)
                        }
                    } else {
                        chooseAsParent(existing)
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun chooseToAddChildAndMerge(newName: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.add_new_material))
            .setMessage(
                getString(
                    R.string.the_material_does_not_exist_would_you_like_to_add_it,
                    newName
                )
            )
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                lifecycleScope.launch {
                    val existing = withContext(Dispatchers.IO) {
                        workOrderViewModel.getMaterialSync(newName)
                    }
                    if (existing == null) {
                        val newMaterial = Material(
                            nf.generateRandomIdAsLong(),
                            newName,
                            0.0,
                            0.0,
                            false,
                            df.getCurrentTimeAsString()
                        )
                        workOrderViewModel.insertMaterial(newMaterial).join()
                        val newlyCreated = withContext(Dispatchers.IO) {
                            workOrderViewModel.getMaterialSync(newName)
                        }
                        if (newlyCreated != null) {
                            chooseAsChild(newlyCreated)
                        }
                    } else {
                        chooseAsChild(existing)
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun chooseOptionsForMerge(parent: Material, child: Material, onMerged: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.choose_merge_option))
            .setMessage(getString(R.string.either_merge_and_replace_or_merge_))
            .setPositiveButton(getString(R.string.merge_and_replace)) { _, _ ->
                mergeAndReplace(parent, child, onMerged)
            }
            .setNeutralButton(getString(R.string.merge_and_keep)) { _, _ ->
                mergeAndKeep(parent, child, onMerged)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun mergeAndReplace(parent: Material, child: Material, onMerged: () -> Unit) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    workOrderViewModel.updateMaterialMerged(
                        child.materialId,
                        parent.materialId,
                        df.getCurrentTimeAsString()
                    ).join()
                    delay(WAIT_100)
                    workOrderViewModel.deleteMaterialMerged(child.materialId).join()
                    workOrderViewModel.deleteMaterial(child.materialId, df.getCurrentTimeAsString())
                        .join()
                }
                onMerged()
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    private fun mergeAndKeep(parent: Material, child: Material, onMerged: () -> Unit) {
        lifecycleScope.launch {
            workOrderViewModel.insertMaterialMerged(
                MaterialMerged(
                    nf.generateRandomIdAsLong(),
                    parent.materialId,
                    child.materialId,
                    false,
                    df.getCurrentTimeAsString()
                )
            )
            onMerged()
        }
    }
}