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
import ms.mattschlenkrich.paydaycalculator.database.repository.EmployerRepository
import ms.mattschlenkrich.paydaycalculator.database.repository.PayDayRepository
import ms.mattschlenkrich.paydaycalculator.database.repository.PayDetailRepository
import ms.mattschlenkrich.paydaycalculator.database.repository.WorkExtraRepository
import ms.mattschlenkrich.paydaycalculator.database.repository.WorkOrderRepository
import ms.mattschlenkrich.paydaycalculator.database.repository.WorkTaxRepository
import ms.mattschlenkrich.paydaycalculator.database.viewModel.EmployerViewModel
import ms.mattschlenkrich.paydaycalculator.database.viewModel.EmployerViewModelFactory
import ms.mattschlenkrich.paydaycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paydaycalculator.database.viewModel.MainViewModelFactory
import ms.mattschlenkrich.paydaycalculator.database.viewModel.PayDayViewModel
import ms.mattschlenkrich.paydaycalculator.database.viewModel.PayDayViewModelFactory
import ms.mattschlenkrich.paydaycalculator.database.viewModel.PayDetailViewModel
import ms.mattschlenkrich.paydaycalculator.database.viewModel.PayDetailViewModelFactory
import ms.mattschlenkrich.paydaycalculator.database.viewModel.WorkExtraViewModel
import ms.mattschlenkrich.paydaycalculator.database.viewModel.WorkExtraViewModelFactory
import ms.mattschlenkrich.paydaycalculator.database.viewModel.WorkOrderViewModel
import ms.mattschlenkrich.paydaycalculator.database.viewModel.WorkOrderViewModelFactory
import ms.mattschlenkrich.paydaycalculator.database.viewModel.WorkTaxViewModel
import ms.mattschlenkrich.paydaycalculator.database.viewModel.WorkTaxViewModelFactory
import ms.mattschlenkrich.paydaycalculator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mView: View
    lateinit var mainViewModel: MainViewModel
    lateinit var employerViewModel: EmployerViewModel
    lateinit var workTaxViewModel: WorkTaxViewModel
    lateinit var workExtraViewModel: WorkExtraViewModel
    lateinit var payDayViewModel: PayDayViewModel
    lateinit var workOrderViewModel: WorkOrderViewModel
    lateinit var payDetailViewModel: PayDetailViewModel
//    private val df = DateFunctions()

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
        setupWorkOrderViewModel()
        setupPayDetailViewModel()
        fillMenus()
    }

    private fun fillMenus() {
        fillTopMenuAndActions()
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

    private fun fillTopMenuAndActions() {
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//                                menu.add(getString(R.string.review_tax_types))
                //                menu.add(getString(R.string.review_extra_credits_deductions))
                //                menu.add(getString(R.string.review_work_extra_frequencies))
                //                menu.add(getString(R.string.update_future_pay_dates))
                menu.add(getString(R.string.view_work_orders))
                menu.add(getString(R.string.view_job_specs))
                menu.add(getString(R.string.view_materials))
                menu.add(resources.getString(R.string.app_name))
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.title) {
//                    getString(R.string.review_tax_types) -> {
//                        gotoTaxTypes()
//                        true
//                    }

                    getString(R.string.view_work_orders) -> {
                        gotoWorkOrders()
                        true
                    }
//
//                    getString(R.string.review_extra_credits_deductions) -> {
//                        gotoExtras()
//                        true
//                    }

                    getString(R.string.view_job_specs) -> {
                        gotoJobSpecs()
                        true
                    }

                    getString(R.string.view_materials) -> {
                        gotoMaterialView()
                        true
                    }

                    else -> {
                        false
                    }
                }

            }
        })
    }

    private fun gotoMaterialView() {
        findNavController(R.id.nav_host_fragment_container).navigate(
            NavGraphDirections.actionGlobalMaterialViewFragment()
        )
    }

    private fun gotoJobSpecs() {
        findNavController(R.id.nav_host_fragment_container).navigate(
            NavGraphDirections.actionGlobalJobSpecViewFragment()
        )
    }

    private fun gotoEmployer() {
        findNavController(R.id.nav_host_fragment_container).navigate(
            NavGraphDirections.actionGlobalEmployerFragment()
        )
    }

    private fun gotoWorkOrders() {
        findNavController(R.id.nav_host_fragment_container).navigate(
            NavGraphDirections.actionGlobalWorkOrdersFragment()
        )
    }

    private fun gotoPayDetails() {
        findNavController(R.id.nav_host_fragment_container).navigate(
            NavGraphDirections.actionGlobalPayDetailFragment()
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

//    private fun gotoTaxTypes() {
//        findNavController(R.id.nav_host_fragment_container).navigate(
//            NavGraphDirections.actionGlobalTaxTypeFragment()
//        )
//    }

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

    private fun setupWorkOrderViewModel() {
        val workOrderRepository =
            WorkOrderRepository(PayDatabase(this))
        val workOrderViewModelFactory =
            WorkOrderViewModelFactory(application, workOrderRepository)
        workOrderViewModel = ViewModelProvider(
            this, workOrderViewModelFactory
        )[WorkOrderViewModel::class.java]
    }

    private fun setupPayDetailViewModel() {
        val payDetailRepository =
            PayDetailRepository(PayDatabase(this))
        val payDetailViewModelFactory =
            PayDetailViewModelFactory(application, payDetailRepository)
        payDetailViewModel = ViewModelProvider(
            this, payDetailViewModelFactory
        )[PayDetailViewModel::class.java]
    }
}