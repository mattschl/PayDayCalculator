package ms.mattschlenkrich.paydaycalculator.ui

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
import ms.mattschlenkrich.paydaycalculator.NavGraphDirections
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.database.PayDatabase
import ms.mattschlenkrich.paydaycalculator.databinding.ActivityMainBinding
import ms.mattschlenkrich.paydaycalculator.repository.EmployerRepository
import ms.mattschlenkrich.paydaycalculator.repository.PayDayRepository
import ms.mattschlenkrich.paydaycalculator.repository.WorkExtraRepository
import ms.mattschlenkrich.paydaycalculator.repository.WorkTaxRepository
import ms.mattschlenkrich.paydaycalculator.viewModel.EmployerViewModel
import ms.mattschlenkrich.paydaycalculator.viewModel.EmployerViewModelFactory
import ms.mattschlenkrich.paydaycalculator.viewModel.MainViewModel
import ms.mattschlenkrich.paydaycalculator.viewModel.MainViewModelFactory
import ms.mattschlenkrich.paydaycalculator.viewModel.PayDayViewModel
import ms.mattschlenkrich.paydaycalculator.viewModel.PayDayViewModelFactory
import ms.mattschlenkrich.paydaycalculator.viewModel.WorkExtraViewModel
import ms.mattschlenkrich.paydaycalculator.viewModel.WorkExtraViewModelFactory
import ms.mattschlenkrich.paydaycalculator.viewModel.WorkTaxViewModel
import ms.mattschlenkrich.paydaycalculator.viewModel.WorkTaxViewModelFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mView: View
    lateinit var mainViewModel: MainViewModel
    lateinit var employerViewModel: EmployerViewModel
    lateinit var workTaxViewModel: WorkTaxViewModel
    lateinit var workExtraViewModel: WorkExtraViewModel
    lateinit var payDayViewModel: PayDayViewModel
//    private val df = DateFunctions()

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
        setupWorkExtraViewModel()
        setupPayDayViewModel()
        fillMenus()
    }

    private fun fillMenus() {
        FillTopMenuAndActions()
        fillBottomNavAndActions()
    }

    private fun fillBottomNavAndActions() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_time_sheet -> {
                    gotoTimeSheet()
                    true
                }

                R.id.nav_pay_details -> {
                    gotoPayDetails()
                    true
                }

                R.id.nav_employer_view -> {
                    gotoEmployer()
                    true
                }

                R.id.nav_tax_rules -> {
                    gotoTaxRules()
                    true
                }

                R.id.nav_extras -> {
                    gotoExtras()
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    private fun FillTopMenuAndActions() {
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                //                menu.add(getString(R.string.review_tax_types))
                //                menu.add(getString(R.string.review_extra_credits_deductions))
                //                menu.add(getString(R.string.review_work_extra_frequencies))
                //                menu.add(getString(R.string.update_future_pay_dates))
                menu.add(resources.getString(R.string.app_name))
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.title) {
                    getString(R.string.review_tax_types) -> {
                        gotoTaxTypes()
                        true
                    }

                    getString(R.string.review_extra_credits_deductions) -> {
                        gotoExtras()
                        true
                    }

                    else -> {
                        false
                    }
                }

            }
        })
    }

    private fun gotoPayDetails() {
        findNavController(R.id.nav_host_fragment_container).navigate(
            NavGraphDirections.actionGlobalPayDetailsFragment()
        )
    }

    private fun gotoTimeSheet() {
        findNavController(R.id.nav_host_fragment_container).navigate(
            NavGraphDirections.actionGlobalTimeSheetFragment()
        )
    }

    private fun gotoExtras() {
        findNavController(R.id.nav_host_fragment_container).navigate(
            NavGraphDirections.actionGlobalEmployerExtraDefinitionsFragment()
        )
    }

    private fun gotoTaxRules() {
        findNavController(R.id.nav_host_fragment_container).navigate(
            NavGraphDirections.actionGlobalTaxRulesFragment()
        )
    }

    private fun gotoTaxTypes() {
        findNavController(R.id.nav_host_fragment_container).navigate(
            NavGraphDirections.actionGlobalTaxTypeFragment()
        )
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


    private fun setupPayDayViewModel() {
        val payDayRepository = PayDayRepository(
            PayDatabase(this)
        )
        val payDayViewModelFactory =
            PayDayViewModelFactory(application, payDayRepository)
        payDayViewModel = ViewModelProvider(
            this,
            payDayViewModelFactory
        )[PayDayViewModel::class.java]
    }

    private fun setupWorkExtraViewModel() {
        val workExtraRepository = WorkExtraRepository(
            PayDatabase(this)
        )
        val workExtraViewModelFactory =
            WorkExtraViewModelFactory(application, workExtraRepository)
        workExtraViewModel = ViewModelProvider(
            this,
            workExtraViewModelFactory
        )[WorkExtraViewModel::class.java]
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