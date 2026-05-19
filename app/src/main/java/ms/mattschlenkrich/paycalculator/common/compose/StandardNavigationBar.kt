package ms.mattschlenkrich.paycalculator.common.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.bottomNavItems
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel

@Composable
fun StandardNavigationBar(
    mainViewModel: MainViewModel,
    navController: NavHostController,
    currentDestination: NavDestination?
) {
    Surface(
        color = NavigationBarDefaults.containerColor,
        tonalElevation = NavigationBarDefaults.Elevation,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            NavigationBar(
                modifier = Modifier.height(56.dp),
                windowInsets = WindowInsets(0, 0, 0, 0),
                tonalElevation = 0.dp
            ) {
                bottomNavItems.forEachIndexed { index, screen ->
                    val isPagerRoute = currentDestination?.route == Screen.MainPager.route
                    val isSelected = if (isPagerRoute) {
                        mainViewModel.selectedTopLevelIndex.intValue == index
                    } else {
                        currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    }

                    NavigationBarItem(
                        icon = {
                            Icon(
                                painterResource(id = screen.icon),
                                contentDescription = stringResource(screen.resourceId),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            if (isPagerRoute) {
                                mainViewModel.setSelectedTopLevelIndex(index)
                            } else {
                                mainViewModel.setSelectedTopLevelIndex(index)
                                navController.navigate(Screen.MainPager.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}