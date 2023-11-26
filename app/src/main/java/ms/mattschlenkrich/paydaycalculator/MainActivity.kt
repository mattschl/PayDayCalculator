package ms.mattschlenkrich.paydaycalculator

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import ms.mattschlenkrich.paydaycalculator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mView: View

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

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.add("${R.string.app_name}")
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                TODO("Not yet implemented")
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
}