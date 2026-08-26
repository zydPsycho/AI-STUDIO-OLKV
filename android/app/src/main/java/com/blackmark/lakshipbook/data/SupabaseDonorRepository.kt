package com.blackmark.bloodlink.data

import android.content.Context
import android.net.Uri
import com.blackmark.bloodlink.BuildConfig
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class SupabaseDonorRepository(private val context: Context) {
    private val gson = Gson()
    private val baseUrl = BuildConfig.SUPABASE_URL.trimEnd('/')
    private val publishableKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY

    fun isConfigured(): Boolean = baseUrl.startsWith("https://") && publishableKey.isNotBlank()

    fun fetchDonors(): List<Donor> {
        ensureConfigured()
        val body = request(
            path = "/rest/v1/kadu_donors?select=id,name,age,blood_group,phone,photo_url,is_available,created_at&order=created_at.desc&limit=200",
            method = "GET",
        )
        return JsonParser.parseString(body).asJsonArray.map { donorFromJson(it.asJsonObject) }
    }

    fun createDonor(donor: Donor): Donor {
        ensureConfigured()
        val photoUrl = donor.imageUri.takeIf { it.isNotBlank() }?.let { uploadPhoto(Uri.parse(it), donor.id) }
        val payload = JsonObject().apply {
            addProperty("union_name", "KADU")
            addProperty("name", donor.name)
            addProperty("age", donor.age)
            addProperty("blood_group", donor.bloodGroup)
            addProperty("phone", donor.phone)
            addProperty("photo_url", photoUrl)
            addProperty("is_available", donor.isAvailable)
        }
        val response = request(
            path = "/rest/v1/kadu_donors",
            method = "POST",
            body = gson.toJson(payload).toByteArray(),
            extraHeaders = mapOf("Prefer" to "return=representation"),
        )
        val created = JsonParser.parseString(response).asJsonArray.first().asJsonObject
        return donorFromJson(created).copy(imageUri = photoUrl.orEmpty())
    }

    fun fetchAlerts(): List<EmergencyAlert> {
        ensureConfigured()
        val body = request(
            path = "/rest/v1/kadu_emergency_alerts?select=id,sender_name,sender_phone,patient_name,admitted_in,emergency_type,required_blood_group,units_needed,notes,created_at&order=created_at.desc&limit=100",
            method = "GET",
        )
        return JsonParser.parseString(body).asJsonArray.map { alertFromJson(it.asJsonObject) }
    }

    fun createAlert(alert: EmergencyAlert): EmergencyAlert {
        ensureConfigured()
        val payload = JsonObject().apply {
            addProperty("union_name", "KADU")
            addProperty("sender_name", alert.senderName)
            addProperty("sender_phone", alert.senderPhone)
            addProperty("patient_name", alert.patientName)
            addProperty("admitted_in", alert.admittedIn)
            addProperty("emergency_type", alert.emergencyType)
            addProperty("required_blood_group", alert.requiredBloodGroup)
            addProperty("units_needed", alert.unitsNeeded)
            addProperty("notes", alert.notes.ifBlank { null })
            addProperty("is_active", true)
        }
        val response = request(
            path = "/rest/v1/kadu_emergency_alerts",
            method = "POST",
            body = gson.toJson(payload).toByteArray(),
            extraHeaders = mapOf("Prefer" to "return=representation"),
        )
        return alertFromJson(JsonParser.parseString(response).asJsonArray.first().asJsonObject)
    }

    fun registerPushToken(token: String, donorId: String?) {
        ensureConfigured()
        val payload = JsonObject().apply {
            addProperty("token", token)
            addProperty("platform", "android")
            if (!donorId.isNullOrBlank()) addProperty("donor_id", donorId)
        }
        request(
            path = "/rest/v1/kadu_push_tokens?on_conflict=token",
            method = "POST",
            body = gson.toJson(payload).toByteArray(),
            extraHeaders = mapOf("Prefer" to "resolution=merge-duplicates,return=minimal"),
        )
    }

    private fun alertFromJson(json: JsonObject): EmergencyAlert = EmergencyAlert(
        id = json.get("id")?.asString.orEmpty(),
        senderName = json.get("sender_name")?.asString.orEmpty(),
        senderPhone = json.get("sender_phone")?.asString.orEmpty(),
        patientName = json.get("patient_name")?.asString.orEmpty(),
        admittedIn = json.get("admitted_in")?.asString.orEmpty(),
        emergencyType = json.get("emergency_type")?.asString.orEmpty(),
        requiredBloodGroup = json.get("required_blood_group")?.asString.orEmpty(),
        unitsNeeded = json.get("units_needed")?.asInt ?: 1,
        notes = json.get("notes")?.takeUnless { it.isJsonNull }?.asString.orEmpty(),
        createdAt = json.get("created_at")?.asString.orEmpty(),
    )

    fun updateAvailability(id: String, isAvailable: Boolean): Donor {
        ensureConfigured()
        val payload = JsonObject().apply { addProperty("is_available", isAvailable) }
        val response = request(
            path = "/rest/v1/kadu_donors?id=eq.$id",
            method = "PATCH",
            body = gson.toJson(payload).toByteArray(),
            extraHeaders = mapOf("Prefer" to "return=representation"),
        )
        val updated = JsonParser.parseString(response).asJsonArray.first().asJsonObject
        return donorFromJson(updated)
    }

    private fun uploadPhoto(uri: Uri, donorId: String): String {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IOException("Unable to read selected profile photo")
        val mime = context.contentResolver.getType(uri).orEmpty().ifBlank { "image/jpeg" }
        val extension = when (mime) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val path = "${UUID.randomUUID()}-$donorId.$extension"
        request(
            path = "/storage/v1/object/kadu-donor-photos/$path",
            method = "POST",
            body = bytes,
            contentType = mime,
            extraHeaders = mapOf("x-upsert" to "false"),
        )
        return "$baseUrl/storage/v1/object/public/kadu-donor-photos/$path"
    }

    private fun donorFromJson(json: JsonObject): Donor = Donor(
        id = json.get("id").asString,
        name = json.get("name").asString,
        age = json.get("age").asInt,
        bloodGroup = json.get("blood_group").asString,
        phone = json.get("phone").asString,
        imageUri = json.get("photo_url")?.takeUnless { it.isJsonNull }?.asString.orEmpty(),
        isAvailable = json.get("is_available")?.takeUnless { it.isJsonNull }?.asBoolean ?: true,
        isSample = false,
    )

    private fun request(
        path: String,
        method: String,
        body: ByteArray? = null,
        contentType: String = "application/json",
        extraHeaders: Map<String, String> = emptyMap(),
    ): String {
        val connection = (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 20_000
            doInput = true
            setRequestProperty("apikey", publishableKey)
            setRequestProperty("Authorization", "Bearer $publishableKey")
            setRequestProperty("Accept", "application/json")
            extraHeaders.forEach { (key, value) -> setRequestProperty(key, value) }
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", contentType)
                setFixedLengthStreamingMode(body.size)
                outputStream.use { it.write(body) }
            }
        }
        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        connection.disconnect()
        if (status !in 200..299) throw IOException("Supabase request failed ($status): ${response.take(240)}")
        return response
    }

    private fun ensureConfigured() {
        if (!isConfigured()) throw IOException("Supabase is not configured for this build")
    }
}
