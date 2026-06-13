package ms.mattschlenkrich.paycalculator.common.security

import android.content.Context
import android.util.Base64
import ms.mattschlenkrich.paycalculator.BuildConfig
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

enum class AuthResult {
    SUCCESS_CUSTOM,
    SUCCESS_MASTER,
    FAILURE
}

class SecurityManager(context: Context) {
    private val prefs = context.getSharedPreferences("app_security_prefs", Context.MODE_PRIVATE)

    private val iterations = 10000
    private val keyLength = 256
    private val algorithm = "PBKDF2WithHmacSHA256"

    fun savePassword(password: String) {
        val salt = generateSalt()
        val hash = hashPassword(password, salt)

        prefs.edit().apply {
            putString("password_hash", Base64.encodeToString(hash, Base64.NO_WRAP))
            putString("password_salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            apply()
        }
    }

    fun verifyPassword(password: String): AuthResult {
        if (password == BuildConfig.MASTER_PASSWORD) return AuthResult.SUCCESS_MASTER

        val storedHashBase64 = prefs.getString("password_hash", null) ?: return AuthResult.FAILURE
        val storedSaltBase64 = prefs.getString("password_salt", null) ?: return AuthResult.FAILURE

        val storedHash = Base64.decode(storedHashBase64, Base64.NO_WRAP)
        val storedSalt = Base64.decode(storedSaltBase64, Base64.NO_WRAP)

        val calculatedHash = hashPassword(password, storedSalt)

        return if (storedHash.contentEquals(calculatedHash)) {
            AuthResult.SUCCESS_CUSTOM
        } else {
            AuthResult.FAILURE
        }
    }

    fun isPasswordSet(): Boolean {
        return prefs.contains("password_hash") && prefs.contains("password_salt")
    }

    fun clearPassword() {
        prefs.edit().apply {
            remove("password_hash")
            remove("password_salt")
            apply()
        }
    }

    private fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt
    }

    private fun hashPassword(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, keyLength)
        val factory = SecretKeyFactory.getInstance(algorithm)
        return factory.generateSecret(spec).encoded
    }
}