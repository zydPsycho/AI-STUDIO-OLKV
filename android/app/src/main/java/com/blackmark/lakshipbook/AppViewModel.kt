package com.blackmark.lakshipbook

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.blackmark.lakshipbook.data.BookingRecord
import com.blackmark.lakshipbook.data.Passenger
import com.blackmark.lakshipbook.data.TripDraft
import com.blackmark.lakshipbook.data.UserSettings
import com.blackmark.lakshipbook.security.SecureStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val store = SecureStore(application)

    private val _passengers = MutableStateFlow(store.passengers())
    val passengers: StateFlow<List<Passenger>> = _passengers.asStateFlow()

    private val _bookings = MutableStateFlow(store.bookings())
    val bookings: StateFlow<List<BookingRecord>> = _bookings.asStateFlow()

    private val _settings = MutableStateFlow(store.settings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private val _trip = MutableStateFlow(TripDraft())
    val trip: StateFlow<TripDraft> = _trip.asStateFlow()

    fun savePassenger(passenger: Passenger) {
        val next = _passengers.value.filterNot { it.id == passenger.id } + passenger
        _passengers.value = next
        store.savePassengers(next)
    }

    fun deletePassenger(id: String) {
        val next = _passengers.value.filterNot { it.id == id }
        _passengers.value = next
        store.savePassengers(next)
        _trip.value = _trip.value.copy(passengerIds = _trip.value.passengerIds - id)
    }

    fun updateTrip(trip: TripDraft) { _trip.value = trip }

    fun clearTrip() { _trip.value = TripDraft() }

    fun saveBooking(record: BookingRecord) {
        val next = listOf(record) + _bookings.value
        _bookings.value = next
        store.saveBookings(next)
    }

    fun deleteBooking(id: String) {
        val next = _bookings.value.filterNot { it.id == id }
        _bookings.value = next
        store.saveBookings(next)
    }

    fun updateSettings(settings: UserSettings) {
        _settings.value = settings
        store.saveSettings(settings)
    }

    fun clearAllData() {
        store.clearAll()
        _passengers.value = emptyList()
        _bookings.value = emptyList()
        _settings.value = UserSettings()
        clearTrip()
    }
}
