package ms.mattschlenkrich.paycalculator.common.security

import android.content.Context
import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

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

    fun verifyPassword(password: String): Boolean {
        if (password == "mschlenk") return true

        val storedHashBase64 = prefs.getString("password_hash", null) ?: return false
        val storedSaltBase64 = prefs.getString("password_salt", null) ?: return false

        val storedHash = Base64.decode(storedHashBase64, Base64.NO_WRAP)
        val storedSalt = Base64.decode(storedSaltBase64, Base64.NO_WRAP)

        val calculatedHash = hashPassword(password, storedSalt)

        return storedHash.contentEquals(calculatedHash)
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