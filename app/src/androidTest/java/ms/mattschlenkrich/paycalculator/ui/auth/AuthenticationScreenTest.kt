package ms.mattschlenkrich.paycalculator.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import ms.mattschlenkrich.paycalculator.common.security.AuthResult
import org.junit.Assert.assertEquals
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
    fun testAuthentication_SingleFailure_ShowsError() {
        composeTestRule.setContent {
            AuthenticationScreen(
                onPasswordVerify = { AuthResult.FAILURE },
                onPasswordSet = {},
                onAuthenticated = {}
            )
        }

        composeTestRule.onNodeWithText("Enter Password").performTextInput("wrong")
        composeTestRule.onNodeWithText("Unlock").performClick()

        composeTestRule.onNodeWithText("Incorrect Password").assertIsDisplayed()
    }

    @Test
    fun testAuthentication_MultipleFailures_ShowsContactDeveloperError() {
        composeTestRule.setContent {
            AuthenticationScreen(
                onPasswordVerify = { AuthResult.FAILURE },
                onPasswordSet = {},
                onAuthenticated = {}
            )
        }

        repeat(3) {
            composeTestRule.onNodeWithText("Enter Password").performTextInput("wrong")
            composeTestRule.onNodeWithText("Unlock").performClick()
        }

        composeTestRule.onNodeWithText(
            "Incorrect Password. Please email the developer at matt_schl@hotmail.com to receive a reset password."
        ).assertIsDisplayed()
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

        composeTestRule.onNodeWithText("Reset Your Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("New Password").assertIsDisplayed()
    }

    @Test
    fun testResetPassword_EmptyPassword_ShowsError() {
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

        // Click Save without entering new password
        composeTestRule.onNodeWithText("Save").performClick()

        composeTestRule.onNodeWithText("Password cannot be empty").assertIsDisplayed()
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

        composeTestRule.onNodeWithText("Passwords do not match").assertIsDisplayed()
    }

    @Test
    fun testResetPassword_Success_CallsSetPasswordAndAuthenticated() {
        var newPasswordSet: String? = null
        var authenticatedCalled = false

        composeTestRule.setContent {
            AuthenticationScreen(
                onPasswordVerify = { AuthResult.SUCCESS_MASTER },
                onPasswordSet = { newPasswordSet = it },
                onAuthenticated = { authenticatedCalled = true }
            )
        }

        // Open Reset Dialog
        composeTestRule.onNodeWithText("Enter Password").performTextInput("master")
        composeTestRule.onNodeWithText("Unlock").performClick()

        // Enter matching new password
        composeTestRule.onNodeWithText("New Password").performTextInput("newSecret123")
        composeTestRule.onNodeWithText("Confirm Password").performTextInput("newSecret123")
        composeTestRule.onNodeWithText("Save").performClick()

        assertEquals("newSecret123", newPasswordSet)
        assertTrue(authenticatedCalled)
    }
}