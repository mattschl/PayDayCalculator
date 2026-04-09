package ms.mattschlenkrich.paycalculator.employer

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
import androidx.navigation.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.Employers
import ms.mattschlenkrich.paycalculator.databinding.FragmentEmployerBinding

//private const val TAG = FRAG_EMPLOYERS

class EmployerFragment : Fragment(R.layout.fragment_employer), SearchView.OnQueryTextListener,
    MenuProvider {

    private var _binding: FragmentEmployerBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var employerViewModel: EmployerViewModel
    private var employerAdapter: EmployerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployerBinding.inflate(inflater, container, false)
        mView = binding.root
        mainActivity = (activity as MainActivity)
        employerViewModel = mainActivity.employerViewModel
        mainActivity.topMenuBar.title = getString(R.string.view_employers)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        populateEmployers()
        val menuHost: MenuHost = mainActivity.topMenuBar
        menuHost.addMenuProvider(
            this, viewLifecycleOwner, Lifecycle.State.RESUMED
        )
        setClickActions()
    }

    private fun populateEmployers() {
        employerAdapter = EmployerAdapter(
            mainActivity,
            mView,
            this@EmployerFragment,
        )
        binding.rvEmployers.apply {
            layoutManager = StaggeredGridLayoutManager(
                2, StaggeredGridLayoutManager.VERTICAL
            )
            setHasFixedSize(true)
            adapter = employerAdapter
        }
        activity?.let {
            employerViewModel.getEmployers().observe(
                viewLifecycleOwner
            ) { employer ->
                employerAdapter!!.differ.submitList(employer)
                updateUI(employer)
            }
        }
    }

    private fun updateUI(employer: List<Employers>?) {
        binding.apply {
            if (employer!!.isEmpty()) {
                rvEmployers.visibility = View.GONE
                crdNoInfo.visibility = View.VISIBLE
            } else {
                rvEmployers.visibility = View.VISIBLE
                crdNoInfo.visibility = View.GONE
            }
        }
    }

    private fun setClickActions() {
        binding.apply {
            fabNew.setOnClickListener {
                gotoEmployerAddFragment()
            }
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

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            searchEmployers(newText)
        }
        return true
    }

    private fun searchEmployers(query: String?) {
        if (employerAdapter != null) {
            val searchQuery = "%$query%"
            employerViewModel.searchEmployers(searchQuery).observe(
                viewLifecycleOwner
            ) { list ->
                employerAdapter!!.differ.submitList(list)
                updateUI(list)
            }
        }
    }

    fun gotoEmployerUpdateFragment() {
        mView.findNavController().navigate(
            EmployerFragmentDirections.actionEmployerFragmentToEmployerUpdateFragment()
        )
    }

    private fun gotoEmployerAddFragment() {
        mView.findNavController().navigate(
            EmployerFragmentDirections.actionEmployerFragmentToEmployerAddFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}