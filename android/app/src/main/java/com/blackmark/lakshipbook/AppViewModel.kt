package com.blackmark.bloodlink

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.blackmark.bloodlink.data.Donor
import com.blackmark.bloodlink.data.EmergencyAlert
import com.blackmark.bloodlink.data.SupabaseDonorRepository
import com.blackmark.bloodlink.security.SecureStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val store = SecureStore(application)
    private val repository = SupabaseDonorRepository(application)

    private val _donors = MutableStateFlow(store.donors())
    val donors: StateFlow<List<Donor>> = _donors.asStateFlow()

    private val _currentDonorId = MutableStateFlow(store.currentDonorId())
    val currentDonorId: StateFlow<String?> = _currentDonorId.asStateFlow()

    private val _alerts = MutableStateFlow<List<EmergencyAlert>>(emptyList())
    val alerts: StateFlow<List<EmergencyAlert>> = _alerts.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        refresh()
        refreshAlerts()
    }

    fun refresh() {
        viewModelScope.launch {
            _isSyncing.value = true
            _error.value = null
            runCatching {
                withContext(Dispatchers.IO) { repository.fetchDonors() }
            }.onSuccess { remoteDonors ->
                val resolvedOwner = resolveOwner(remoteDonors)
                val resolvedOwnerId = resolvedOwner?.id
                _currentDonorId.value = resolvedOwnerId
                store.saveCurrentDonorId(resolvedOwnerId)
                _donors.value = remoteDonors
                store.saveDonors(remoteDonors)
                refreshAlerts()
            }.onFailure { error ->
                _error.value = error.message ?: "Could not connect to the KADU donor directory."
            }
            _isSyncing.value = false
        }
    }

    fun saveDonor(donor: Donor, onResult: (success: Boolean, duplicatePhone: Boolean) -> Unit = { _, _ -> }) {
        val localDonor = donor.copy(isSample = false)
        val optimistic = _donors.value.filterNot { it.id == localDonor.id } + localDonor
        _donors.value = optimistic
        store.saveDonors(optimistic)
        _currentDonorId.value = localDonor.id
        store.saveCurrentDonorId(localDonor.id)
        viewModelScope.launch {
            _isSyncing.value = true
            _error.value = null
            runCatching {
                withContext(Dispatchers.IO) { repository.createDonor(localDonor) }
            }.onSuccess { syncedDonor ->
                val next = _donors.value.filterNot { it.id == localDonor.id || it.id == syncedDonor.id } + syncedDonor
                _donors.value = next
                store.saveDonors(next)
                _currentDonorId.value = syncedDonor.id
                store.saveCurrentDonorId(syncedDonor.id)
                onResult(true, false)
            }.onFailure { error ->
                val next = _donors.value.filterNot { it.id == localDonor.id }
                _donors.value = next
                store.saveDonors(next)
                _currentDonorId.value = null
                store.saveCurrentDonorId(null)
                val duplicatePhone = error.message.orEmpty().contains("kadu_donors_normalized_phone_uidx") || error.message.orEmpty().contains("duplicate key")
                _error.value = if (duplicatePhone) "A KADU profile already uses this phone number. Load that existing profile instead." else error.message ?: "We could not publish your profile. Please try again."
                onResult(false, duplicatePhone)
            }
            _isSyncing.value = false
        }
    }

    fun refreshAlerts() {
        viewModelScope.launch {
            val bloodGroup = _donors.value.firstOrNull { it.id == _currentDonorId.value }?.bloodGroup
            runCatching { withContext(Dispatchers.IO) { repository.fetchAlerts(bloodGroup) } }
                .onSuccess { _alerts.value = it }
                .onFailure { error -> _error.value = error.message ?: "Could not load emergency alerts." }
        }
    }

    fun registerPushToken(token: String) {
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { repository.registerPushToken(token, _currentDonorId.value) } }
                .onFailure { /* Push registration is best-effort; the alert feed remains available. */ }
        }
    }

    fun publishAlert(alert: EmergencyAlert) {
        viewModelScope.launch {
            _isSyncing.value = true
            _error.value = null
            runCatching { withContext(Dispatchers.IO) { repository.createAlert(alert) } }
                .onSuccess { created ->
                    if (_donors.value.firstOrNull { it.id == _currentDonorId.value }?.bloodGroup == created.requiredBloodGroup) {
                        _alerts.value = listOf(created) + _alerts.value
                    }
                }
                .onFailure { error -> _error.value = error.message ?: "Could not send the KADU emergency alert." }
            _isSyncing.value = false
        }
    }

    fun updateAvailability(id: String, isAvailable: Boolean) {
        val current = _donors.value.firstOrNull { it.id == id } ?: return
        val optimistic = current.copy(isAvailable = isAvailable)
        _donors.value = _donors.value.map { if (it.id == id) optimistic else it }
        store.saveDonors(_donors.value)
        viewModelScope.launch {
            _isSyncing.value = true
            _error.value = null
            runCatching {
                withContext(Dispatchers.IO) { repository.updateAvailability(id, isAvailable) }
            }.onSuccess { updated ->
                _donors.value = _donors.value.map { if (it.id == id) updated else it }
                store.saveDonors(_donors.value)
            }.onFailure { error ->
                _donors.value = _donors.value.map { if (it.id == id) current else it }
                store.saveDonors(_donors.value)
                _error.value = error.message ?: "Could not update availability."
            }
            _isSyncing.value = false
        }
    }

    fun recoverProfile(phone: String, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _isSyncing.value = true
            _error.value = null
            runCatching {
                val availableDonors = if (_donors.value.isEmpty()) {
                    repository.fetchDonors().also {
                        _donors.value = it
                        store.saveDonors(it)
                    }
                } else _donors.value
                val normalizedPhone = normalizedPhone(phone)
                availableDonors.firstOrNull { donor -> normalizedPhone(donor.phone) == normalizedPhone }
                    ?: throw IllegalArgumentException("No KADU profile was found with that phone number. Create a new profile if this number is not registered.")
            }.onSuccess { candidate ->
                _currentDonorId.value = candidate.id
                store.saveCurrentDonorId(candidate.id)
                refreshAlerts()
                onResult(true, null)
            }.onFailure { error ->
                val message = error.message ?: "Could not restore your profile."
                _error.value = message
                onResult(false, message)
            }
            _isSyncing.value = false
        }
    }

    fun deleteLocalProfile(id: String) {
        val next = _donors.value.filterNot { it.id == id }
        _donors.value = next
        store.saveDonors(next)
        if (_currentDonorId.value == id) {
            _currentDonorId.value = null
            store.saveCurrentDonorId(null)
        }
    }

    fun clearLocalData() {
        store.clearAll()
        _donors.value = emptyList()
        _currentDonorId.value = null
    }

    private fun resolveOwner(remoteDonors: List<Donor>): Donor? {
        val savedOwner = _donors.value.firstOrNull { it.id == _currentDonorId.value }
        return remoteDonors.firstOrNull { it.id == _currentDonorId.value }
            ?: savedOwner?.let { cached ->
                remoteDonors.firstOrNull { it.samePersonAs(cached) }
            }
    }

    fun dismissError() {
        _error.value = null
    }
}

private fun Donor.samePersonAs(other: Donor): Boolean {
    val normalizedPhone = normalizedPhone(phone)
    val normalizedOtherPhone = normalizedPhone(other.phone)
    return normalizedPhone.length >= 7 && normalizedPhone == normalizedOtherPhone &&
        name.trim().replace(Regex("\\s+"), " ").equals(other.name.trim().replace(Regex("\\s+"), " "), ignoreCase = true)
}

private fun normalizedPhone(phone: String): String {
    val digits = phone.filter(Char::isDigit)
    return if (digits.length >= 10) digits.takeLast(10) else digits
}
