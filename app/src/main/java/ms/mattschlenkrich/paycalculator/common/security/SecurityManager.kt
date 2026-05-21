package ms.mattschlenkrich.paycalculator.common.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurityManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun savePassword(password: String) {
        sharedPreferences.edit().putString("app_password", password).apply()
    }

    fun getPassword(): String? {
        return sharedPreferences.getString("app_password", null)
    }

    fun isPasswordSet(): Boolean {
        return getPassword() != null
    }

    fun verifyPassword(password: String): Boolean {
        return getPassword() == password
    }

    fun clearPassword() {
        sharedPreferences.edit().remove("app_password").apply()
    }
}