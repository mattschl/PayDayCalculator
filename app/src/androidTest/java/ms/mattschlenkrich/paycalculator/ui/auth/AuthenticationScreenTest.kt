package ms.mattschlenkrich.paycalculator.ui.auth

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import ms.mattschlenkrich.paycalculator.common.security.AuthResult
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AuthenticationScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAuthentication_Success_CallsOnAuthenticated() {
        var authenticatedCalled = false

        composeTestRule.setContent {
            AuthenticationScreen(
                onPasswordVerify = { AuthResult.SUCCESS_CUSTOM },
                onPasswordSet = {},
                onAuthenticated = { authenticatedCalled = true }
            )
        }

        composeTestRule.onNodeWithText("Enter Password").performTextInput("any")
        composeTestRule.onNodeWithText("Unlock").performClick()

        assertTrue(authenticatedCalled)
    }

    @Test
    fun testAuthentication_Failure_ShowsError() {
        composeTestRule.setContent {
            AuthenticationScreen(
                onPasswordVerify = { AuthResult.FAILURE },
                onPasswordSet = {},
                onAuthenticated = {}
            )
        }

        composeTestRule.onNodeWithText("Enter Password").performTextInput("wrong")
        composeTestRule.onNodeWithText("Unlock").performClick()

        composeTestRule.onNodeWithText("Incorrect Password").assertExists()
    }

    @Test
    fun testAuthentication_MasterPassword_ShowsResetDialog() {
        composeTestRule.setContent {
            AuthenticationScreen(
                onPasswordVerify = { AuthResult.SUCCESS_MASTER },
                onPasswordSet = {},
                onAuthenticated = {}
            )
        }

        composeTestRule.onNodeWithText("Enter Password").performTextInput("master")
        composeTestRule.onNodeWithText("Unlock").performClick()

        composeTestRule.onNodeWithText("Reset Your Password").assertExists()
        composeTestRule.onNodeWithText("New Password").assertExists()
    }

    @Test
    fun testResetPassword_Mismatch_ShowsError() {
        composeTestRule.setContent {
            AuthenticationScreen(
                onPasswordVerify = { AuthResult.SUCCESS_MASTER },
                onPasswordSet = {},
                onAuthenticated = {}
            )
        }

        // Open Reset Dialog
        composeTestRule.onNodeWithText("Enter Password").performTextInput("master")
        composeTestRule.onNodeWithText("Unlock").performClick()

        // Fill mismatching passwords
        composeTestRule.onNodeWithText("New Password").performTextInput("pass1")
        composeTestRule.onNodeWithText("Confirm Password").performTextInput("pass2")
        composeTestRule.onNodeWithText("Save").performClick()

        composeTestRule.onNodeWithText("Passwords do not match").assertExists()
    }
}