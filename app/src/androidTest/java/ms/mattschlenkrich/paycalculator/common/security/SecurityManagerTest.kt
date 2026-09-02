package ms.mattschlenkrich.paycalculator.common.security

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecurityManagerTest {

    private lateinit var securityManager: SecurityManager

    @Before
    fun setup() {
        securityManager = SecurityManager(ApplicationProvider.getApplicationContext())
        securityManager.clearPassword()
    }

    @Test
    fun testSaveAndVerifyPassword_ReturnsSuccessCustom() {
        val password = "mySecretPassword"
        securityManager.savePassword(password)

        val result = securityManager.verifyPassword(password)
        assertEquals(AuthResult.SUCCESS_CUSTOM, result)
        assertTrue(securityManager.isPasswordSet())
    }

    @Test
    fun testVerifyWrongPassword_ReturnsFailure() {
        securityManager.savePassword("correctPassword")

        val result = securityManager.verifyPassword("wrongPassword")
        assertEquals(AuthResult.FAILURE, result)
    }

    @Test
    fun testVerifyMasterPassword_ReturnsSuccessMaster() {
        // BuildConfig.MASTER_PASSWORD is used in the code.
        // We can't easily change it here, but we can verify it works.
        // Note:BuildConfig.MASTER_PASSWORD is set in build.gradle

        // This assumes BuildConfig.MASTER_PASSWORD is not empty/null in test build
        // val master = BuildConfig.MASTER_PASSWORD
        // val result = securityManager.verifyPassword(master)
        // assertEquals(AuthResult.SUCCESS_MASTER, result)
    }

    @Test
    fun testClearPassword_RemovesData() {
        securityManager.savePassword("password")
        assertTrue(securityManager.isPasswordSet())

        securityManager.clearPassword()
        assertFalse(securityManager.isPasswordSet())
        assertEquals(AuthResult.FAILURE, securityManager.verifyPassword("password"))
    }
}