package com.blackmark.lakshipbook.data

import java.util.UUID

data class Passenger(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val mobile: String = "",
    val email: String = "",
    val address: String = "",
    val idType: String = "",
    val idNumber: String = "",
    val nationality: String = "Indian"
)

data class TripDraft(
    val from: String = "",
    val to: String = "",
    val journeyDate: String = "",
    val passengerIds: List<String> = emptyList(),
    val category: String = "Standard"
)

data class BookingRecord(
    val id: String = UUID.randomUUID().toString(),
    val reference: String = "",
    val bookingDate: String = "",
    val route: String = "",
    val journeyDate: String = "",
    val passengerNames: List<String> = emptyList(),
    val amount: String = "",
    val status: String = "Pending confirmation",
    val officialUrl: String = "https://lakshadweep.irctc.co.in/"
)

data class UserSettings(
    val biometricLockEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true
)
