package ms.mattschlenkrich.paycalculator.ui

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testNavigateToSettings() {
        val context = composeTestRule.activity

        // 1. Click Menu icon in TopAppBar
        composeTestRule.onNodeWithContentDescription("Menu").performClick()

        // 2. Click Settings in Dropdown
        val settingsLabel = context.getString(R.string.settings)
        composeTestRule.onNodeWithText(settingsLabel).performClick()

        // 3. Verify on Settings screen
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodes(hasText(settingsLabel)).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(settingsLabel).assertExists()
    }

    @Test
    fun testNavigateToTaxes() {
        val context = composeTestRule.activity
        val taxesLabel = context.getString(R.string.taxes)
        composeTestRule.onNodeWithText(taxesLabel).performClick()

        val taxTypeLabel = context.getString(R.string.tax_type)
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodes(hasText(taxTypeLabel)).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(taxTypeLabel).assertExists()
    }
}