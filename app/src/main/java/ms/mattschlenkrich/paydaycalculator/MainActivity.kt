package ms.mattschlenkrich.paydaycalculator

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import ms.mattschlenkrich.paydaycalculator.database.PayDatabase
import ms.mattschlenkrich.paydaycalculator.databinding.ActivityMainBinding
import ms.mattschlenkrich.paydaycalculator.repository.EmployerRepository
import ms.mattschlenkrich.paydaycalculator.repository.WorkTaxRepository
import ms.mattschlenkrich.paydaycalculator.viewModel.EmployerViewModel
import ms.mattschlenkrich.paydaycalculator.viewModel.EmployerViewModelFactory
import ms.mattschlenkrich.paydaycalculator.viewModel.MainViewModel
import ms.mattschlenkrich.paydaycalculator.viewModel.MainViewModelFactory
import ms.mattschlenkrich.paydaycalculator.viewModel.WorkTaxViewModel
import ms.mattschlenkrich.paydaycalculator.viewModel.WorkTaxViewModelFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mView: View
    lateinit var employerViewModel: EmployerViewModel
    lateinit var mainViewModel: MainViewModel
    lateinit var workTaxViewModel: WorkTaxViewModel

    private fun gotoEmployer() {
        findNavController(R.id.nav_host_fragment_container).navigate(
            NavGraphDirections.actionGlobalEmployerFragment()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        mView = binding.root
        setContentView(mView)
        setupMainViewModel()
        setupEmployerViewModel()
        setupWorkTaxViewModel()
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.add(resources.getString(R.string.app_name))
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                //set action
                return false
            }
        })
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_employer_view -> {
                    gotoEmployer()
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    private fun setupMainViewModel() {
        val mainViewModelFactory =
            MainViewModelFactory(application)
        mainViewModel = ViewModelProvider(
            this,
            mainViewModelFactory
        )[MainViewModel::class.java]
    }

    private fun setupEmployerViewModel() {
        val employerRepository = EmployerRepository(
            PayDatabase(this)
        )
        val employerViewModelFactory =
            EmployerViewModelFactory(application, employerRepository)
        employerViewModel = ViewModelProvider(
            this, employerViewModelFactory
        )[EmployerViewModel::class.java]
    }

    private fun setupWorkTaxViewModel() {
        val workTaxRepository = WorkTaxRepository(PayDatabase(this))
        val workTaxViewModelFactory =
            WorkTaxViewModelFactory(application, workTaxRepository)
        workTaxViewModel = ViewModelProvider(
            this, workTaxViewModelFactory
        )[WorkTaxViewModel::class.java]
    }
}