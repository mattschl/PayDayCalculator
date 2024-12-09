package ms.mattschlenkrich.paydaycalculator.ui.workorder.materials

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.FRAG_MATERIAL_VIEW
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.Material
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentRecyclerViewBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import ms.mattschlenkrich.paydaycalculator.ui.workorder.materials.adapter.MaterialViewAdapter

private const val TAG = FRAG_MATERIAL_VIEW

class MaterialViewFragment :
    Fragment(R.layout.fragment_recycler_view),
    SearchView.OnQueryTextListener,
    MenuProvider {

    private var _binding: FragmentRecyclerViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private var materialViewAdapter: MaterialViewAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecyclerViewBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = "View Job Spec List"
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        populateMaterials()
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(
            this, viewLifecycleOwner, Lifecycle.State.RESUMED
        )
        setBaseView()
    }

    private fun setBaseView() {
        binding.apply {
            tvNoInfo.text = "No Materials to view"
            fabNew.visibility = View.GONE
        }
    }

    private fun populateMaterials() {
        materialViewAdapter = MaterialViewAdapter(
            mainActivity, mView, TAG
        )
        binding.rvRecycler.apply {
            layoutManager = StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
            setHasFixedSize(true)
            adapter = materialViewAdapter
        }
        activity.let {
            mainActivity.workOrderViewModel.getMaterialsList().observe(
                viewLifecycleOwner
            ) { materialList ->
                materialViewAdapter!!.differ.submitList(materialList)
                updateUI(materialList)
            }
        }
    }

    private fun updateUI(materialList: List<Material>?) {
        binding.apply {
            if (materialList!!.isEmpty()) {
                rvRecycler.visibility = View.GONE
                crdNoInfo.visibility = View.VISIBLE
            } else {
                rvRecycler.visibility = View.VISIBLE
                crdNoInfo.visibility = View.GONE
            }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null) {
            searchMaterial(query)
        }
        return true
    }

    private fun searchMaterial(query: String) {
        if (materialViewAdapter != null) {
            val searchQuery = "%$query%"
            mainActivity.workOrderViewModel.searchMaterials(
                searchQuery
            ).observe(viewLifecycleOwner) { list ->
                materialViewAdapter!!.differ.submitList(list)
                updateUI(list)
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.search_menu, menu)
        val mMenuSearch = menu.findItem(R.id.menu_search)
            .actionView as SearchView
        mMenuSearch.isSubmitButtonEnabled = false
        mMenuSearch.setOnQueryTextListener(this)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }
}