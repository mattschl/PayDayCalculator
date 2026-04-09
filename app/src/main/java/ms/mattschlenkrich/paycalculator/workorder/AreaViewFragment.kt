package ms.mattschlenkrich.paycalculator.workorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.FRAG_AREA_VIEW
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentRecyclerViewBinding

private const val TAG = FRAG_AREA_VIEW

class AreaViewFragment : Fragment(R.layout.fragment_recycler_view), SearchView.OnQueryTextListener,
    MenuProvider {
    private var _binding: FragmentRecyclerViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private lateinit var areaViewAdapter: AreaViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecyclerViewBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        workOrderViewModel = mainActivity.workOrderViewModel
        mainActivity.topMenuBar.title = getString(R.string.view_areas_list)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        populateAreaList()
        val menuHost = mainActivity.topMenuBar
        menuHost.addMenuProvider(
            this, viewLifecycleOwner, Lifecycle.State.RESUMED
        )
        setBaseView()
    }

    private fun populateAreaList() {
        areaViewAdapter = AreaViewAdapter(
            mainActivity, mView, TAG, this@AreaViewFragment,
        )
        binding.rvRecycler.apply {
            layoutManager = StaggeredGridLayoutManager(
                2, StaggeredGridLayoutManager.VERTICAL
            )
            setHasFixedSize(true)
            adapter = areaViewAdapter
        }
        workOrderViewModel.getAreasList().observe(
            viewLifecycleOwner
        ) { areaList ->
            areaViewAdapter.differ.submitList(areaList)
            updateUI(areaList)
        }
    }

    private fun updateUI(workPerformedList: List<Any>?) {
        binding.apply {
            if (workPerformedList!!.isEmpty()) {
                rvRecycler.visibility = View.GONE
                crdNoInfo.visibility = View.VISIBLE
            } else {
                rvRecycler.visibility = View.VISIBLE
                crdNoInfo.visibility = View.GONE
            }
        }
    }

    private fun setBaseView() {
        binding.apply {
            tvNoInfo.text = getString(R.string.no_areas_in_the_list_to_view)
            fabNew.visibility = View.GONE
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.search_menu, menu)
        val mMenuSearch = menu.findItem(R.id.menu_search).actionView as SearchView
        mMenuSearch.isSubmitButtonEnabled = false
        mMenuSearch.setOnQueryTextListener(this)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null) {
            searchAreas(query)
        }
        return true
    }

    private fun searchAreas(query: String) {
        val searchQuery = "%$query%"
        workOrderViewModel.searchAreas(searchQuery).observe(viewLifecycleOwner) { list ->
            areaViewAdapter.differ.submitList(list)
            updateUI(list)
        }
    }

    fun gotoAreaUpdateFragment() {
        mView.findNavController().navigate(
            AreaViewFragmentDirections.actionAreaViewFragmentToAreaUpdateFragment()
        )

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}