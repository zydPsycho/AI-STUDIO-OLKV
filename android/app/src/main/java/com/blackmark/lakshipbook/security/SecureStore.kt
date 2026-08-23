package com.blackmark.lakshipbook.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.blackmark.lakshipbook.data.BookingRecord
import com.blackmark.lakshipbook.data.Passenger
import com.blackmark.lakshipbook.data.UserSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SecureStore(context: Context) {
    private val gson = Gson()
    private val preferences = EncryptedSharedPreferences.create(
        context,
        "lak_ship_book_secure",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun passengers(): List<Passenger> = readList(KEY_PASSENGERS)

    fun savePassengers(value: List<Passenger>) {
        preferences.edit().putString(KEY_PASSENGERS, gson.toJson(value)).apply()
    }

    fun bookings(): List<BookingRecord> = readList(KEY_BOOKINGS)

    fun saveBookings(value: List<BookingRecord>) {
        preferences.edit().putString(KEY_BOOKINGS, gson.toJson(value)).apply()
    }

    fun settings(): UserSettings = preferences.getString(KEY_SETTINGS, null)?.let {
        gson.fromJson(it, UserSettings::class.java)
    } ?: UserSettings()

    fun saveSettings(value: UserSettings) {
        preferences.edit().putString(KEY_SETTINGS, gson.toJson(value)).apply()
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
        const val KEY_PASSENGERS = "passengers"
        const val KEY_BOOKINGS = "bookings"
        const val KEY_SETTINGS = "settings"
    }
}
