package ms.mattschlenkrich.paydaycalculator.ui.employer

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
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.adapter.EmployerAdapter
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentEmployerBinding
import ms.mattschlenkrich.paydaycalculator.model.Employers

class EmployerFragment :
    Fragment(R.layout.fragment_employer),
    SearchView.OnQueryTextListener,
    MenuProvider {

    private var _binding: FragmentEmployerBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var employerAdapter: EmployerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployerBinding.inflate(inflater, container, false)
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.view_employers)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fillEmployers()
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        setActions()
    }

    private fun setActions() {
        binding.apply {
            fabNew.setOnClickListener {
                mView.findNavController().navigate(
                    EmployerFragmentDirections
                        .actionEmployerFragmentToEmployerAddFragment()
                )
            }
        }
    }

    private fun fillEmployers() {
        employerAdapter = EmployerAdapter(
            mainActivity, mView
        )
        binding.rvEmployers.apply {
            layoutManager = StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
            setHasFixedSize(true)
            adapter = employerAdapter
        }
        activity?.let {
            mainActivity.employerViewModel.getEmployers().observe(
                viewLifecycleOwner
            ) { employer ->
                employerAdapter.differ.submitList(employer)
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

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            searchEmployers(newText)
        }
        return true
    }

    private fun searchEmployers(query: String?) {
        val searchQuery = "%$query%"
        mainActivity.employerViewModel.searchEmployers(searchQuery).observe(
            viewLifecycleOwner
        ) { list ->
            employerAdapter.differ.submitList(list)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}