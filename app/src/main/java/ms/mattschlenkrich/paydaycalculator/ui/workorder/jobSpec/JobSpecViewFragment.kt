package ms.mattschlenkrich.paydaycalculator.ui.workorder.jobSpec

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
import ms.mattschlenkrich.paydaycalculator.common.FRAG_JOB_SPEC_VIEW
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.JobSpec
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentRecyclerViewBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import ms.mattschlenkrich.paydaycalculator.ui.workorder.jobSpec.adapter.JobSpecAdapter

private val TAG = FRAG_JOB_SPEC_VIEW

class JobSpecViewFragment :
    Fragment(R.layout.fragment_recycler_view),
    SearchView.OnQueryTextListener,
    MenuProvider {

    private var _binding: FragmentRecyclerViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private var jobSpecAdapter: JobSpecAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecyclerViewBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.view_job_spec_list)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        populateJobSpecs()
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(
            this, viewLifecycleOwner, Lifecycle.State.RESUMED
        )
        setBaseView()
    }

    private fun setBaseView() {
        binding.apply {
            tvNoInfo.text = getString(R.string.no_job_specs_to_view)
            fabNew.visibility = View.GONE
        }
    }

    private fun populateJobSpecs() {
        jobSpecAdapter = JobSpecAdapter(
            mainActivity, mView, TAG
        )
        binding.rvRecycler.apply {
            layoutManager = StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
            setHasFixedSize(true)
            adapter = jobSpecAdapter
        }
        activity?.let {
            mainActivity.workOrderViewModel.getJobSpecsAll().observe(
                viewLifecycleOwner
            ) { jobSpecs ->
                jobSpecAdapter!!.differ.submitList(jobSpecs)
                updateUI(jobSpecs)
            }
        }
    }

    private fun updateUI(jobSpecs: List<JobSpec>?) {
        binding.apply {
            if (jobSpecs!!.isEmpty()) {
                rvRecycler.visibility = View.GONE
                crdNoInfo.visibility = View.VISIBLE
            } else {
                rvRecycler.visibility = View.VISIBLE
                crdNoInfo.visibility = View.GONE
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

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null) {
            searchJobSpecs(query)
        }
        return true
    }

    private fun searchJobSpecs(query: String) {
        if (jobSpecAdapter != null) {
            val searchQuery = "%$query%"
            mainActivity.workOrderViewModel.searchJobSpecs(
                searchQuery
            ).observe(viewLifecycleOwner) { list ->
                jobSpecAdapter!!.differ.submitList(list)
                updateUI(list)
            }
        }
    }
}