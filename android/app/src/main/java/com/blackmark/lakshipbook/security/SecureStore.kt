package com.blackmark.bloodlink.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.blackmark.bloodlink.data.Donor
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SecureStore(context: Context) {
    private val gson = Gson()
    private val preferences = EncryptedSharedPreferences.create(
        context,
        "bloodlink_kavaratti_secure",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun donors(): List<Donor> = readList(KEY_DONORS)

    fun saveDonors(value: List<Donor>) {
        preferences.edit().putString(KEY_DONORS, gson.toJson(value)).apply()
    }

    fun currentDonorId(): String? = preferences.getString(KEY_CURRENT_DONOR, null)

    fun saveCurrentDonorId(id: String?) {
        preferences.edit().apply {
            if (id == null) remove(KEY_CURRENT_DONOR) else putString(KEY_CURRENT_DONOR, id)
        }.apply()
    }

    fun clearAll() {
        preferences.edit().clear().apply()
    }

    private inline fun <reified T> readList(key: String): List<T> {
        val json = preferences.getString(key, null) ?: return emptyList()
        val type = TypeToken.getParameterized(List::class.java, T::class.java).type
        return runCatching { gson.fromJson<List<T>>(json, type) }.getOrDefault(emptyList())
    }

    private companion object {
        const val KEY_DONORS = "donors"
        const val KEY_CURRENT_DONOR = "current_donor"
    }
}
