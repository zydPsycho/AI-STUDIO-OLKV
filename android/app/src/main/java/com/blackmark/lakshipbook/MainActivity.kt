package com.blackmark.bloodlink

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.blackmark.bloodlink.data.BloodGroups
import com.blackmark.bloodlink.data.Donor
import com.blackmark.bloodlink.data.EmergencyAlert
import com.blackmark.bloodlink.ui.theme.BloodLinkTheme
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { BloodLinkTheme { BloodLinkRoot(viewModel) } }
    }
}

private enum class AppTab(val label: String) { DONORS("Donors"), ALERTS("Alerts"), PROFILE("My profile"), SETTINGS("Settings") }

@Composable
private fun BloodLinkRoot(viewModel: AppViewModel) {
    val donors by viewModel.donors.collectAsStateWithLifecycle()
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    val currentDonorId by viewModel.currentDonorId.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.DONORS) }
    var selectedDonor by remember { mutableStateOf<Donor?>(null) }
    var editingDonor by remember { mutableStateOf<Donor?>(null) }
    var showEditor by rememberSaveable { mutableStateOf(false) }
    var showAlertEditor by rememberSaveable { mutableStateOf(false) }
    var selectedAlert by remember { mutableStateOf<EmergencyAlert?>(null) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let { snackbar.showSnackbar(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, modifier = Modifier.navigationBarsPadding()) {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                when (tab) {
                                    AppTab.DONORS -> Icons.Filled.Bloodtype
                                    AppTab.ALERTS -> Icons.Filled.WarningAmber
                                    AppTab.PROFILE -> Icons.Filled.Person
                                    AppTab.SETTINGS -> Icons.Filled.Info
                                },
                                contentDescription = tab.label,
                            )
                        },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                AppTab.DONORS -> DonorDirectoryScreen(
                    donors = donors,
                    currentDonor = donors.firstOrNull { it.id == currentDonorId },
                    isSyncing = isSyncing,
                    onRefresh = viewModel::refresh,
                    onOpenDonor = { selectedDonor = it },
                    onCreateProfile = { editingDonor = null; showEditor = true },
                    onOpenMyProfile = { selectedTab = AppTab.PROFILE },
                )
                AppTab.ALERTS -> EmergencyAlertsScreen(
                    alerts = alerts,
                    isSyncing = isSyncing,
                    onRefresh = viewModel::refreshAlerts,
                    onCreate = { showAlertEditor = true },
                    onOpenAlert = { selectedAlert = it },
                )
                AppTab.PROFILE -> MyProfileScreen(
                    donor = donors.firstOrNull { it.id == currentDonorId },
                    onCreateProfile = { editingDonor = null; showEditor = true },
                )
                AppTab.SETTINGS -> SettingsScreen(
                    donor = donors.firstOrNull { it.id == currentDonorId },
                    isSyncing = isSyncing,
                    onRefresh = viewModel::refresh,
                    onAvailabilityChanged = { donor, value -> viewModel.updateAvailability(donor.id, value) },
                )
            }
        }
    }

    if (showAlertEditor) {
        EmergencyAlertEditor(
            senderName = donors.firstOrNull { it.id == currentDonorId }?.name.orEmpty(),
            senderPhone = donors.firstOrNull { it.id == currentDonorId }?.phone.orEmpty(),
            onDismiss = { showAlertEditor = false },
            onPublish = { alert ->
                viewModel.publishAlert(alert)
                showAlertEditor = false
                selectedTab = AppTab.ALERTS
            },
        )
    }
    if (showEditor) {
        DonorEditor(
            initial = editingDonor,
            onDismiss = { showEditor = false },
            onSave = { donor ->
                viewModel.saveDonor(donor)
                showEditor = false
                selectedTab = AppTab.PROFILE
            },
        )
    }
    selectedDonor?.let { donor -> DonorDetailSheet(donor, onDismiss = { selectedDonor = null }) }
    selectedAlert?.let { alert -> EmergencyAlertDetailSheet(alert, onDismiss = { selectedAlert = null }) }
}

@Composable
private fun ScreenHeader(eyebrow: String, title: String, subtitle: String? = null, action: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(eyebrow.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(5.dp))
            Text(title, style = MaterialTheme.typography.headlineSmall)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        action?.invoke()
    }
}

@Composable
private fun DonorDirectoryScreen(donors: List<Donor>, currentDonor: Donor?, isSyncing: Boolean, onRefresh: () -> Unit, onOpenDonor: (Donor) -> Unit, onCreateProfile: () -> Unit, onOpenMyProfile: () -> Unit) {
    var search by rememberSaveable { mutableStateOf("") }
    var selectedGroup by rememberSaveable { mutableStateOf("All") }
    val filtered = donors.filter { donor ->
        !donor.matchesOwner(currentDonor) &&
            (search.isBlank() || donor.name.contains(search, true)) &&
            (selectedGroup == "All" || donor.bloodGroup == selectedGroup)
    }
    val availableCount = filtered.count { it.isAvailable }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            ScreenHeader(
                "BLOODLINK by KADU",
                "Find a blood donor",
                "KADU union community · Kavaratti, Lakshadweep",
                action = {
                    Row {
                        IconButton(onClick = onRefresh) { if (isSyncing) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp) else Icon(Icons.Filled.Refresh, "Refresh directory") }
                        IconButton(onClick = if (currentDonor == null) onCreateProfile else onOpenMyProfile) { if (currentDonor == null) Icon(Icons.Filled.PersonAdd, "Create donor profile", tint = MaterialTheme.colorScheme.primary) else ProfileAvatar(currentDonor, 30.dp) }
                    }
                },
            )
        }
        item {
            Card(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(52.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) { Icon(Icons.Filled.FavoriteBorder, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(14.dp)) }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("KADU members helping KADU members.", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(3.dp))
                        Text("Names, ages, blood groups and phone numbers are visible in this union directory.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            OutlinedTextField(search, { search = it }, Modifier.padding(horizontal = 20.dp).fillMaxWidth(), singleLine = true, label = { Text("Search by donor name") }, leadingIcon = { Icon(Icons.Filled.Person, null) }, trailingIcon = { if (search.isNotBlank()) IconButton(onClick = { search = "" }) { Icon(Icons.Filled.Close, "Clear search") } }, shape = RoundedCornerShape(16.dp))
        }
        item {
            LazyRow(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterChip(selectedGroup == "All", { selectedGroup = "All" }, label = { Text("All") }) }
                items(BloodGroups) { group -> FilterChip(selectedGroup == group, { selectedGroup = group }, label = { Text(group) }) }
            }
        }
        item {
            Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${filtered.size} donor${if (filtered.size == 1) "" else "s"}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(8.dp)); Text("·", color = MaterialTheme.colorScheme.outline); Spacer(Modifier.width(8.dp))
                Text("$availableCount available now", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            }
        }
        if (filtered.isEmpty()) {
            item {
                Card(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Bloodtype, null, Modifier.size(38.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(10.dp)); Text(if (donors.isEmpty()) "No donor profiles yet" else "No matching donors", style = MaterialTheme.typography.titleMedium)
                        Text(if (donors.isEmpty()) "Add the first KADU donor profile from your union." else "Try another name or blood group.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        if (currentDonor == null) { Spacer(Modifier.height(16.dp)); Button(onClick = onCreateProfile) { Icon(Icons.Filled.Add, null); Spacer(Modifier.width(6.dp)); Text("ADD PROFILE") } } else { Spacer(Modifier.height(16.dp)); Text("Your profile is hidden from your donor list.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center) }
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { donor -> DonorCard(donor, onClick = { onOpenDonor(donor) }) }
        }
        
    }
}

@Composable
private fun DonorCard(donor: Donor, onClick: () -> Unit) {
    Card(Modifier.padding(horizontal = 20.dp).fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            ProfileAvatar(donor, 58.dp); Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(donor.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) { Text("${donor.age} years", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(6.dp)); Text("·", color = MaterialTheme.colorScheme.outline); Spacer(Modifier.width(6.dp)); Icon(Icons.Filled.LocationOn, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant); Text("Kavaratti", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Spacer(Modifier.height(8.dp)); AvailabilityLabel(donor.isAvailable)
            }
            BloodGroupBadge(donor.bloodGroup)
        }
    }
}

@Composable
private fun BloodGroupBadge(group: String) { Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) { Text(group, Modifier.padding(horizontal = 11.dp, vertical = 8.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer) } }

@Composable
private fun AvailabilityLabel(isAvailable: Boolean) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.CheckCircle, null, Modifier.size(15.dp), tint = if (isAvailable) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline); Spacer(Modifier.width(5.dp)); Text(if (isAvailable) "Available now" else "Not available", style = MaterialTheme.typography.labelMedium, color = if (isAvailable) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant) } }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DonorDetailSheet(donor: Donor, onDismiss: () -> Unit) {
    val context = LocalContext.current
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).navigationBarsPadding().padding(bottom = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { ProfileAvatar(donor, 78.dp); Spacer(Modifier.width(16.dp)); Column(Modifier.weight(1f)) { Text(donor.name, style = MaterialTheme.typography.headlineSmall); Text("KADU · Kavaratti", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(8.dp)); AvailabilityLabel(donor.isAvailable) }; BloodGroupBadge(donor.bloodGroup) }
            Spacer(Modifier.height(22.dp)); HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant); Spacer(Modifier.height(18.dp)); Text("Donor details", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(10.dp))
            DetailRow("Age", "${donor.age} years", Icons.Filled.Person); DetailRow("Blood group", donor.bloodGroup, Icons.Filled.Bloodtype); DetailRow("Phone", donor.phone, Icons.Filled.Phone)
            Spacer(Modifier.height(18.dp)); Button(onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${donor.phone}"))) }, Modifier.fillMaxWidth().height(52.dp)) { Icon(Icons.Filled.Call, null); Spacer(Modifier.width(8.dp)); Text("CALL ${donor.name.uppercase()}") }; Spacer(Modifier.height(10.dp)); OutlinedButton(onClick = onDismiss, Modifier.fillMaxWidth()) { Text("DONE") }
        }
    }
}

@Composable
private fun EmergencyAlertsScreen(
    alerts: List<EmergencyAlert>,
    isSyncing: Boolean,
    onRefresh: () -> Unit,
    onCreate: () -> Unit,
    onOpenAlert: (EmergencyAlert) -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            ScreenHeader(
                "KADU emergency network",
                "Emergency alerts",
                "Notify matching blood-group donors in the KADU directory.",
                action = { IconButton(onClick = onRefresh) { if (isSyncing) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp) else Icon(Icons.Filled.Refresh, "Refresh alerts") } },
            )
        }
        item {
            Card(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.WarningAmber, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(30.dp)); Spacer(Modifier.width(10.dp)); Text("Need blood urgently?", style = MaterialTheme.typography.titleLarge) }
                    Spacer(Modifier.height(8.dp))
                    Text("Create one alert with the patient, hospital, blood group and contact details. Only donors with the selected blood group will see it.", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onCreate, Modifier.fillMaxWidth()) { Icon(Icons.Filled.Add, null); Spacer(Modifier.width(8.dp)); Text("CREATE EMERGENCY ALERT") }
                }
            }
        }
        item { Text("Active alerts", Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.titleLarge) }
        if (alerts.isEmpty()) {
            item { Card(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.CheckCircle, null, Modifier.size(38.dp), tint = MaterialTheme.colorScheme.secondary); Spacer(Modifier.height(10.dp)); Text("No active emergency alerts", style = MaterialTheme.typography.titleMedium); Text("The KADU community has no current blood requests.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
        } else {
            items(alerts, key = { it.id }) { alert -> EmergencyAlertCard(alert, onClick = { onOpenAlert(alert) }) }
        }
        item { Text("Alerts are for KADU union coordination. For life-threatening situations, contact the hospital or emergency services directly.", Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun EmergencyAlertCard(alert: EmergencyAlert, onClick: () -> Unit) {
    Card(Modifier.padding(horizontal = 20.dp).fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.45f))) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.Top) { Column(Modifier.weight(1f)) { Text("${alert.requiredBloodGroup} blood needed", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error); Text("${alert.unitsNeeded} unit${if (alert.unitsNeeded == 1) "" else "s"} · ${alert.emergencyType}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Icon(Icons.Filled.WarningAmber, null, tint = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(12.dp)); Text("Patient: ${alert.patientName}", style = MaterialTheme.typography.titleMedium); Text("Admitted at: ${alert.admittedIn}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(10.dp)); Text("Posted by ${alert.senderName}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary); Spacer(Modifier.height(10.dp)); OutlinedButton(onClick = onClick, Modifier.fillMaxWidth()) { Text("VIEW ALERT & CONTACT") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmergencyAlertEditor(senderName: String, senderPhone: String, onDismiss: () -> Unit, onPublish: (EmergencyAlert) -> Unit) {
    var author by rememberSaveable(senderName) { mutableStateOf(senderName) }
    var phone by rememberSaveable(senderPhone) { mutableStateOf(senderPhone) }
    var patient by rememberSaveable { mutableStateOf("") }
    var hospital by rememberSaveable { mutableStateOf("") }
    var emergencyType by rememberSaveable { mutableStateOf("") }
    var bloodGroup by rememberSaveable { mutableStateOf("") }
    var units by rememberSaveable { mutableStateOf("1") }
    var notes by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = MaterialTheme.colorScheme.background) {
        LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Column(Modifier.weight(1f)) { Text("Create emergency alert", style = MaterialTheme.typography.headlineSmall); Text("Notify matching blood-group donors", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }; IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, "Close") } } }
            item { FormField("Your name", author) { author = it; error = null } }
            item { FormField("Your phone number", phone, keyboardType = KeyboardType.Phone) { phone = it; error = null } }
            item { FormField("Patient name", patient) { patient = it; error = null } }
            item { FormField("Admitted hospital / location", hospital) { hospital = it; error = null } }
            item { FormField("Emergency type", emergencyType) { emergencyType = it; error = null } }
            item { Column { Text("Required blood group", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(5.dp)); LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { items(BloodGroups) { group -> FilterChip(bloodGroup == group, { bloodGroup = group; error = null }, label = { Text(group) }) } } } }
            item { FormField("Units needed (1–20)", units, keyboardType = KeyboardType.Number) { units = it.filter(Char::isDigit); error = null } }
            item { OutlinedTextField(value = notes, onValueChange = { notes = it }, Modifier.fillMaxWidth(), label = { Text("Notes (optional)") }, minLines = 3, maxLines = 5, shape = RoundedCornerShape(14.dp)) }
            if (error != null) item { Text(error.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
            item { Button(onClick = { val parsedUnits = units.toIntOrNull(); error = when { author.trim().length < 2 -> "Please enter your name."; phone.filter(Char::isDigit).length < 7 -> "Please enter a valid phone number."; patient.trim().length < 2 -> "Please enter the patient name."; hospital.trim().length < 2 -> "Please enter the admitted hospital or location."; emergencyType.trim().length < 2 -> "Please enter the emergency type."; bloodGroup !in BloodGroups -> "Please select the required blood group."; parsedUnits == null || parsedUnits !in 1..20 -> "Units must be between 1 and 20."; else -> null }; if (error == null) onPublish(EmergencyAlert(senderName = author.trim(), senderPhone = phone.trim(), patientName = patient.trim(), admittedIn = hospital.trim(), emergencyType = emergencyType.trim(), requiredBloodGroup = bloodGroup, unitsNeeded = parsedUnits!!, notes = notes.trim())) }, Modifier.fillMaxWidth().height(52.dp)) { Icon(Icons.Filled.WarningAmber, null); Spacer(Modifier.width(8.dp)); Text("NOTIFY MATCHING DONORS") } }
            item { Text("Only create an alert for a genuine urgent blood requirement. Your name and phone number will be visible to matching KADU donors.", Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmergencyAlertDetailSheet(alert: EmergencyAlert, onDismiss: () -> Unit) {
    val context = LocalContext.current
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).navigationBarsPadding().padding(bottom = 20.dp)) {
            Row(verticalAlignment = Alignment.Top) { Column(Modifier.weight(1f)) { Text("Emergency blood request", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.error); Text("${alert.requiredBloodGroup} · ${alert.unitsNeeded} unit${if (alert.unitsNeeded == 1) "" else "s"}", style = MaterialTheme.typography.titleMedium) }; Icon(Icons.Filled.WarningAmber, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp)) }
            Spacer(Modifier.height(18.dp)); DetailRow("Patient", alert.patientName, Icons.Filled.Person); DetailRow("Admitted in", alert.admittedIn, Icons.Filled.LocationOn); DetailRow("Emergency", alert.emergencyType, Icons.Filled.WarningAmber); if (alert.notes.isNotBlank()) DetailRow("Notes", alert.notes, Icons.Filled.Info); DetailRow("Alert sender", "${alert.senderName} · ${alert.senderPhone}", Icons.Filled.Phone)
            Spacer(Modifier.height(16.dp)); Button(onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${alert.senderPhone}"))) }, Modifier.fillMaxWidth().height(52.dp)) { Icon(Icons.Filled.Call, null); Spacer(Modifier.width(8.dp)); Text("I CAN HELP · CALL ${alert.senderName.uppercase()}") }; Spacer(Modifier.height(10.dp)); OutlinedButton(onClick = { playEmergencyTone(context) }, Modifier.fillMaxWidth()) { Icon(Icons.Filled.VolumeUp, null); Spacer(Modifier.width(8.dp)); Text("PLAY EMERGENCY TONE") }; Spacer(Modifier.height(10.dp)); OutlinedButton(onClick = onDismiss, Modifier.fillMaxWidth()) { Text("CLOSE") }
        }
    }
}

private fun playEmergencyTone(context: Context) {
    MediaPlayer.create(context, com.blackmark.bloodlink.R.raw.emergency_alert)?.also { player -> player.setOnCompletionListener { it.release() }; player.start() }
}

@Composable
private fun DetailRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) { Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, Modifier.size(21.dp), tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(12.dp)); Column { Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value, style = MaterialTheme.typography.bodyLarge) } } }

@Composable
private fun MyProfileScreen(donor: Donor?, onCreateProfile: () -> Unit, onEdit: () -> Unit = {}, onDelete: () -> Unit = {}) {
    if (donor == null) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { ScreenHeader("Your profile", "Add your KADU donor card", "Your profile will be visible in the shared directory") }
            item { Card(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(24.dp)) { Column(Modifier.padding(22.dp)) { Icon(Icons.Filled.PersonAdd, null, Modifier.size(34.dp), tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.height(12.dp)); Text("Help your union find you.", style = MaterialTheme.typography.titleLarge); Spacer(Modifier.height(6.dp)); Text("Add your name, age, blood group, phone number and photo. All member details are visible because this directory is for KADU union use only.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(18.dp)); Button(onClick = onCreateProfile, Modifier.fillMaxWidth().height(50.dp)) { Text("CREATE MY PROFILE") } } } }
        }
        return
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { ScreenHeader("Your profile", "My donor card", "Your local profile is shown here only") }
        item { Card(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(20.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { ProfileAvatar(donor, 72.dp); Spacer(Modifier.width(16.dp)); Column(Modifier.weight(1f)) { Text(donor.name, style = MaterialTheme.typography.titleLarge); Text("${donor.age} years · KADU · Kavaratti", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(8.dp)); AvailabilityLabel(donor.isAvailable) }; BloodGroupBadge(donor.bloodGroup) }; Spacer(Modifier.height(18.dp)); HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant); Spacer(Modifier.height(12.dp)); DetailRow("Phone", donor.phone, Icons.Filled.Phone) } } }
    }
}

@Composable
private fun UnionNotice() { Card(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(18.dp)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) { Icon(Icons.Filled.Security, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(12.dp)); Column { Text("KADU union directory", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(4.dp)); Text("This app has no login. Anyone who receives the app or public link can view the directory, so share it only within KADU. Phone numbers are intentionally visible for union coordination.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } } } }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DonorEditor(initial: Donor?, onDismiss: () -> Unit, onSave: (Donor) -> Unit) {
    var name by rememberSaveable(initial?.id) { mutableStateOf(initial?.name.orEmpty()) }
    var age by rememberSaveable(initial?.id) { mutableStateOf(if ((initial?.age ?: 0) > 0) initial?.age.toString() else "") }
    var bloodGroup by rememberSaveable(initial?.id) { mutableStateOf(initial?.bloodGroup.orEmpty()) }
    var phone by rememberSaveable(initial?.id) { mutableStateOf(initial?.phone.orEmpty()) }
    var imageUri by rememberSaveable(initial?.id) { mutableStateOf(initial?.imageUri.orEmpty()) }
    var error by rememberSaveable(initial?.id) { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> if (uri != null) imageUri = copyPickedImage(context, uri) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = MaterialTheme.colorScheme.background) {
        LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Column(Modifier.weight(1f)) { Text(if (initial == null) "Create donor profile" else "Edit donor profile", style = MaterialTheme.typography.headlineSmall); Text("BLOODLINK by KADU · Kavaratti", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }; IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, "Close") } } }
            item { PhotoPickerAvatar(imageUri, name, onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) }
            item { FormField("Full name", name) { name = it; error = null } }
            item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { FormField("Age", age, Modifier.weight(0.8f), KeyboardType.Number) { age = it.filter(Char::isDigit); error = null }; Column(Modifier.weight(1.6f)) { Text("Blood group", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(5.dp)); LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { items(BloodGroups) { group -> FilterChip(bloodGroup == group, { bloodGroup = group; error = null }, label = { Text(group) }) } } } } }
            item { FormField("Phone number", phone, keyboardType = KeyboardType.Phone) { phone = it; error = null } }
            item { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(18.dp)) { Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Visibility, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(12.dp)); Column { Text("Phone visible to union members", style = MaterialTheme.typography.titleMedium); Text("No login is used in this KADU directory.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Spacer(Modifier.weight(1f)); Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.secondary) } } }
            if (error != null) item { Text(error.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
            item { Button(onClick = { val parsedAge = age.toIntOrNull(); error = when { name.trim().length < 2 -> "Please enter your name."; parsedAge == null || parsedAge !in 18..70 -> "Please enter an age between 18 and 70."; bloodGroup !in BloodGroups -> "Please select your blood group."; phone.trim().replace(" ", "").length < 7 -> "Please enter a valid phone number."; else -> null }; if (error == null) onSave(Donor(initial?.id ?: UUID.randomUUID().toString(), name.trim(), parsedAge!!, bloodGroup, phone.trim(), imageUri, true, true, false)) }, Modifier.fillMaxWidth().height(52.dp)) { Text(if (initial == null) "PUBLISH TO KADU DIRECTORY" else "SAVE CHANGES") } }
            item { Text("By publishing, you confirm these details are intended for KADU union use.", Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun FormField(label: String, value: String, modifier: Modifier = Modifier, keyboardType: KeyboardType = KeyboardType.Text, onValueChange: (String) -> Unit) { OutlinedTextField(value, onValueChange, modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = keyboardType), shape = RoundedCornerShape(14.dp)) }

@Composable
private fun PhotoPickerAvatar(imageUri: String, name: String, onClick: () -> Unit) { Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) { Box(contentAlignment = Alignment.BottomEnd) { ProfileAvatar(Donor(name = name, imageUri = imageUri), 104.dp); Surface(modifier = Modifier.size(34.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) { IconButton(onClick = onClick) { Icon(Icons.Filled.PhotoCamera, "Choose profile photo", tint = MaterialTheme.colorScheme.onPrimary) } } }; Spacer(Modifier.height(8.dp)); Text("Add a profile photo", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary) } }

@Composable
private fun ProfileAvatar(donor: Donor, size: androidx.compose.ui.unit.Dp) {
    val context = LocalContext.current
    val cachedPhoto by produceState<String?>(initialValue = null, key1 = donor.imageUri) {
        value = if (donor.imageUri.isBlank()) null else withContext(Dispatchers.IO) { cachePhoto(context, donor.imageUri) }
    }
    if (cachedPhoto != null) {
        val request = remember(cachedPhoto) { ImageRequest.Builder(context).data(cachedPhoto).crossfade(true).allowHardware(false).build() }
        AsyncImage(model = request, contentDescription = "Profile photo of ${donor.name}", modifier = Modifier.size(size).clip(CircleShape), contentScale = ContentScale.Crop)
    } else {
        Surface(modifier = Modifier.size(size), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) { Box(contentAlignment = Alignment.Center) { Text(initials(donor.name), style = if (size > 80.dp) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold) } }
    }
}

private fun cachePhoto(context: Context, source: String): String? {
    if (source.startsWith("file:") || source.startsWith("content:")) return source
    if (!source.startsWith("https://") && !source.startsWith("http://")) return null
    val target = File(context.cacheDir, "bloodlink-photo-${Integer.toHexString(source.hashCode())}.img")
    if (target.exists() && target.length() > 0L) return Uri.fromFile(target).toString()
    return runCatching {
        val connection = (URL(source).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 20_000
            requestMethod = "GET"
            setRequestProperty("Accept", "image/*")
        }
        try {
            if (connection.responseCode !in 200..299) return null
            val temporary = File(target.parentFile, "${target.name}.part")
            connection.inputStream.use { input -> temporary.outputStream().use { output -> input.copyTo(output) } }
            if (temporary.length() == 0L) { temporary.delete(); return null }
            if (!temporary.renameTo(target)) { temporary.copyTo(target, overwrite = true); temporary.delete() }
            Uri.fromFile(target).toString()
        } finally {
            connection.disconnect()
        }
    }.getOrNull()
}

private fun copyPickedImage(context: Context, uri: Uri): String {
    return runCatching {
        val extension = when (context.contentResolver.getType(uri)) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val file = File(context.cacheDir, "bloodlink-profile-${UUID.randomUUID()}.$extension")
        context.contentResolver.openInputStream(uri)?.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
            ?: return uri.toString()
        Uri.fromFile(file).toString()
    }.getOrElse { uri.toString() }
}

private fun Donor.matchesOwner(owner: Donor?): Boolean {
    if (owner == null) return false
    val phoneDigits = phone.filter(Char::isDigit)
    val ownerPhoneDigits = owner.phone.filter(Char::isDigit)
    val normalizedName = name.trim().replace(Regex("\\s+"), " ")
    val normalizedOwnerName = owner.name.trim().replace(Regex("\\s+"), " ")
    return id == owner.id || (phoneDigits.length >= 7 && phoneDigits == ownerPhoneDigits && normalizedName.equals(normalizedOwnerName, ignoreCase = true))
}

private fun initials(name: String): String = name.trim().split(Regex("\\s+"))
.filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "?" }

@Composable
private fun SettingsScreen(donor: Donor?, isSyncing: Boolean, onRefresh: () -> Unit, onAvailabilityChanged: (Donor, Boolean) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { ScreenHeader("Settings", "Your availability", "Update only your own donor status", action = { IconButton(onClick = onRefresh) { if (isSyncing) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp) else Icon(Icons.Filled.Refresh, "Refresh directory") } }) }
        if (donor == null) {
            item { Card(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(22.dp)) { Text("Create your profile first", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(6.dp)); Text("Your availability setting will appear here after you publish your KADU donor profile.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
        } else {
            item { Card(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp)) { Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Available to donate", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(4.dp)); Text(if (donor.isAvailable) "Your profile is marked available in the directory." else "Your profile is marked not available.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Switch(checked = donor.isAvailable, onCheckedChange = { onAvailabilityChanged(donor, it) }) } } }
        }
        item { Text("BLOODLINK by KADU · Kavaratti, Lakshadweep · v1.0", Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun AboutScreen(onClearData: () -> Unit) {
    var showClearDialog by rememberSaveable { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { ScreenHeader("About the union directory", "BLOODLINK by KADU", "Simple donor coordination for KADU members") }
        item { Card(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(20.dp)) { Text("How it works", style = MaterialTheme.typography.titleLarge); Spacer(Modifier.height(12.dp)); AboutStep("01", "Create a profile", "Add your name, age, blood group, phone number and an optional photo."); AboutStep("02", "Search by blood group", "Use the blood-group filter to find a matching KADU donor quickly."); AboutStep("03", "Call directly", "Phone numbers are visible for union coordination and the call button opens your dialer.") } } }
        item { Button(onClick = { showClearDialog = true }, Modifier.padding(horizontal = 20.dp).fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Icon(Icons.Filled.DeleteOutline, null); Spacer(Modifier.width(8.dp)); Text("CLEAR THIS DEVICE") } }
        item { Text("BLOODLINK by KADU · Kavaratti, Lakshadweep · v1.0", Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
    if (showClearDialog) AlertDialog(onDismissRequest = { showClearDialog = false }, title = { Text("Clear this device?") }, text = { Text("This removes the locally saved donor profile. It does not delete the shared Supabase profile.") }, confirmButton = { TextButton(onClick = { onClearData(); showClearDialog = false }) { Text("CLEAR") } }, dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("CANCEL") } })
}

@Composable
private fun AboutStep(number: String, title: String, body: String) { Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.Top) { Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) { Text(number, Modifier.padding(horizontal = 10.dp, vertical = 7.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer) }; Spacer(Modifier.width(12.dp)); Column { Text(title, style = MaterialTheme.typography.titleMedium); Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
