package com.blackmark.bloodlink.data

import java.util.UUID

data class Donor(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val age: Int = 0,
    val bloodGroup: String = "",
    val phone: String = "",
    val imageUri: String = "",
    val isAvailable: Boolean = true,
    val sharePhone: Boolean = false,
    val isSample: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
)

val BloodGroups = listOf("A+", "A−", "B+", "B−", "O+", "O−", "AB+", "AB−")

data class EmergencyAlert(
    val id: String = "",
    val senderName: String = "",
    val senderPhone: String = "",
    val patientName: String = "",
    val admittedIn: String = "",
    val emergencyType: String = "",
    val requiredBloodGroup: String = "",
    val unitsNeeded: Int = 1,
    val notes: String = "",
    val createdAt: String = "",
)
