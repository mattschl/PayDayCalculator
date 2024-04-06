package ms.mattschlenkrich.paydaycalculator.ui.tax

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
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.adapter.TaxTypeAdapter
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentTaxTypeBinding
import ms.mattschlenkrich.paydaycalculator.model.tax.TaxTypes
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

//const val TAG = FRAG_TAX_RULES

class TaxTypeFragment :
    Fragment(R.layout.fragment_tax_type),
    SearchView.OnQueryTextListener,
    MenuProvider {

    private var _binding: FragmentTaxTypeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var taxTypeAdapter: TaxTypeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaxTypeBinding.inflate(inflater, container, false)
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.choose_a_tax_type)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fillTaxTypeList()
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        setActions()
    }

    private fun setActions() {
        binding.apply {
            fabNew.setOnClickListener {
                mView.findNavController().navigate(
                    TaxTypeFragmentDirections
                        .actionTaxTypeFragmentToTaxTypeAddFragment()
                )
            }
        }
    }

    private fun fillTaxTypeList() {
        taxTypeAdapter = TaxTypeAdapter(
            mainActivity, mView
        )
        binding.rvTaxTypes.apply {
            layoutManager = StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
            setHasFixedSize(true)
            adapter = taxTypeAdapter
        }
        activity?.let {
            mainActivity.workTaxViewModel.getTaxTypes().observe(
                viewLifecycleOwner
            ) { taxTypes ->
                taxTypeAdapter.differ.submitList(taxTypes)
                updateUI(taxTypes)
            }
        }
    }

    private fun updateUI(taxTypes: List<TaxTypes>) {
        binding.apply {
            if (taxTypes.isEmpty()) {
                crdNoInfo.visibility = View.VISIBLE
                rvTaxTypes.visibility = View.GONE
            } else {
                crdNoInfo.visibility = View.GONE
                rvTaxTypes.visibility = View.VISIBLE
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
            searchTaxTypes(newText)
        }
        return true
    }

    private fun searchTaxTypes(query: String) {
        val searchQuery = "%$query%"
        mainActivity.workTaxViewModel.searchTaxTypes(searchQuery).observe(
            viewLifecycleOwner
        ) { list ->
            taxTypeAdapter.differ.submitList(list)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}