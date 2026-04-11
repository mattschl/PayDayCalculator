package ms.mattschlenkrich.paycalculator

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import ms.mattschlenkrich.paycalculator.data.EmployerRepository
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.EmployerViewModelFactory
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.MainViewModelFactory
import ms.mattschlenkrich.paycalculator.data.PayCalculationsRepository
import ms.mattschlenkrich.paycalculator.data.PayCalculationsViewModel
import ms.mattschlenkrich.paycalculator.data.PayCalculationsViewModelFactory
import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.PayDayRepository
import ms.mattschlenkrich.paycalculator.data.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.PayDayViewModelFactory
import ms.mattschlenkrich.paycalculator.data.PayDetailRepository
import ms.mattschlenkrich.paycalculator.data.PayDetailViewModel
import ms.mattschlenkrich.paycalculator.data.PayDetailViewModelFactory
import ms.mattschlenkrich.paycalculator.data.WorkExtraRepository
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModelFactory
import ms.mattschlenkrich.paycalculator.data.WorkOrderRepository
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModelFactory
import ms.mattschlenkrich.paycalculator.data.WorkTaxRepository
import ms.mattschlenkrich.paycalculator.data.WorkTaxViewModel
import ms.mattschlenkrich.paycalculator.data.WorkTaxViewModelFactory
import ms.mattschlenkrich.paycalculator.data.WorkTimeRepository
import ms.mattschlenkrich.paycalculator.data.WorkTimeViewModel
import ms.mattschlenkrich.paycalculator.data.WorkTimeViewModelFactory
import ms.mattschlenkrich.paycalculator.databinding.ActivityMainBinding
import ms.mattschlenkrich.paycalculator.sync.SyncActivity

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
    lateinit var payCalculationsViewModel: PayCalculationsViewModel
    lateinit var workTimeViewModel: WorkTimeViewModel
    lateinit var topMenuBar: Toolbar
//    private val df = DateFunctions()

    private val syncLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        mView = binding.root
        topMenuBar = binding.topMenu
        setContentView(mView)
        setupMainViewModel()
        setupEmployerViewModel()
        setupWorkTaxViewModel()
        setupWorkExtraViewModel()
        setupPayDayViewModel()
        setupWorkOrderViewModel()
        setupPayDetailViewModel()
        setupPayCalculationsViewModel()
        setupWorTimeViewModel()
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
        topMenuBar.apply {
            menu.add(getString(R.string.view_work_order_list))
            menu.add(getString(R.string.view_job_spec_list))
            menu.add(getString(R.string.view_areas_list))
            menu.add(getString(R.string.view_work_performed_list))
            menu.add(getString(R.string.view_material_list))
            menu.add(getString(R.string.sync_data))
            menu.add(resources.getString(R.string.app_name))

            setOnMenuItemClickListener { menuItem ->
                when (menuItem.title) {

                    getString(R.string.view_work_order_list) -> {
                        gotoWorkOrders()
                        true
                    }

                    getString(R.string.view_work_performed_list) -> {
                        gotoWorkPerformedView()
                        true
                    }

                    getString(R.string.view_areas_list) -> {
                        gotoAreasView()
                        true
                    }

                    getString(R.string.view_job_spec_list) -> {
                        gotoJobSpecs()
                        true
                    }

                    getString(R.string.view_material_list) -> {
                        gotoMaterialView()
                        true
                    }

                    getString(R.string.sync_data) -> {
                        gotoSync()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }
    }

    private fun gotoSync() {
        val intent = Intent(this, SyncActivity::class.java)
        syncLauncher.launch(intent)
    }

    private fun gotoAreasView() {
        findNavController(R.id.nav_host_fragment_container).navigate(
            NavGraphDirections.actionGlobalAreaViewFragment()
        )
    }

    private fun gotoWorkPerformedView() {
        findNavController(R.id.nav_host_fragment_container).navigate(
            NavGraphDirections.actionGlobalWorkPerformedViewFragment()
        )
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
            NavGraphDirections.actionGlobalPayDetailFragmentNew()
        )
//        findNavController(R.id.nav_host_fragment_container).navigate(
//            NavGraphDirections.actionGlobalPayDetailFragment()
//        )
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


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    private fun setupMainViewModel() {
        val mainViewModelFactory = MainViewModelFactory(application)
        mainViewModel = ViewModelProvider(
            this, mainViewModelFactory
        )[MainViewModel::class.java]
    }


    private fun setupPayDayViewModel() {
        val payDayRepository = PayDayRepository(
            PayDatabase(this)
        )
        val payDayViewModelFactory = PayDayViewModelFactory(application, payDayRepository)
        payDayViewModel = ViewModelProvider(
            this, payDayViewModelFactory
        )[PayDayViewModel::class.java]
    }

    private fun setupWorkExtraViewModel() {
        val workExtraRepository = WorkExtraRepository(
            PayDatabase(this)
        )
        val workExtraViewModelFactory = WorkExtraViewModelFactory(application, workExtraRepository)
        workExtraViewModel = ViewModelProvider(
            this, workExtraViewModelFactory
        )[WorkExtraViewModel::class.java]
    }

    private fun setupEmployerViewModel() {
        val employerRepository = EmployerRepository(
            PayDatabase(this)
        )
        val employerViewModelFactory = EmployerViewModelFactory(application, employerRepository)
        employerViewModel = ViewModelProvider(
            this, employerViewModelFactory
        )[EmployerViewModel::class.java]
    }

    private fun setupWorkTaxViewModel() {
        val workTaxRepository = WorkTaxRepository(PayDatabase(this))
        val workTaxViewModelFactory = WorkTaxViewModelFactory(application, workTaxRepository)
        workTaxViewModel = ViewModelProvider(
            this, workTaxViewModelFactory
        )[WorkTaxViewModel::class.java]
    }

    private fun setupWorkOrderViewModel() {
        val workOrderRepository = WorkOrderRepository(PayDatabase(this))
        val workOrderViewModelFactory = WorkOrderViewModelFactory(application, workOrderRepository)
        workOrderViewModel = ViewModelProvider(
            this, workOrderViewModelFactory
        )[WorkOrderViewModel::class.java]
    }

    private fun setupPayDetailViewModel() {
        val payDetailRepository = PayDetailRepository(PayDatabase(this))
        val payDetailViewModelFactory = PayDetailViewModelFactory(application, payDetailRepository)
        payDetailViewModel = ViewModelProvider(
            this, payDetailViewModelFactory
        )[PayDetailViewModel::class.java]
    }

    private fun setupPayCalculationsViewModel() {
        val payCalculationsRepository = PayCalculationsRepository(PayDatabase(this))
        val payCalculationsViewModelFactory =
            PayCalculationsViewModelFactory(application, payCalculationsRepository)
        payCalculationsViewModel = ViewModelProvider(
            this, payCalculationsViewModelFactory
        )[PayCalculationsViewModel::class.java]
    }

    private fun setupWorTimeViewModel() {
        val workTimeRepository = WorkTimeRepository(PayDatabase(this))
        val workTimeViewModelFactory = WorkTimeViewModelFactory(application, workTimeRepository)
        workTimeViewModel = ViewModelProvider(
            this, workTimeViewModelFactory
        )[WorkTimeViewModel::class.java]
    }
}