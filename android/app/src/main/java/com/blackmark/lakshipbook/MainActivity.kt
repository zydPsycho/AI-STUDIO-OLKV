package com.blackmark.lakshipbook

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookOnline
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blackmark.lakshipbook.data.BookingRecord
import com.blackmark.lakshipbook.data.Passenger
import com.blackmark.lakshipbook.data.TripDraft
import com.blackmark.lakshipbook.data.UserSettings
import com.blackmark.lakshipbook.ui.theme.LakShipBookTheme
import com.blackmark.lakshipbook.web.FormAssistant
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : FragmentActivity() {
    private val viewModel by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LakShipBookTheme { LakShipBookRoot(viewModel) } }
    }
}

private enum class AppTab(val label: String) { HOME("Home"), PASSENGERS("Passengers"), BOOKINGS("Bookings"), SETTINGS("Settings") }

@Composable
private fun LakShipBookRoot(viewModel: AppViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var unlocked by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    fun authenticate() {
        val activity = context as? FragmentActivity ?: return
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { unlocked = true }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { unlocked = false }
        })
        prompt.authenticate(BiometricPrompt.PromptInfo.Builder().setTitle("Unlock LAK SHIP BOOK").setSubtitle("Protect saved passenger profiles").setNegativeButtonText("CANCEL").build())
    }
    LaunchedEffect(settings.biometricLockEnabled) {
        if (!settings.biometricLockEnabled) unlocked = true else if (!unlocked) authenticate()
    }
    if (settings.biometricLockEnabled && !unlocked) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Lock, null, Modifier.size(54.dp), tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(18.dp))
                Text("LAK SHIP BOOK is locked", style = MaterialTheme.typography.headlineSmall)
                Text("Use your device biometric or PIN to continue.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(18.dp))
                Button(onClick = { authenticate() }) { Text("UNLOCK") }
            }
        }
        return
    }
    LakShipBookContent(viewModel)
}

@Composable
private fun LakShipBookContent(viewModel: AppViewModel) {
    var tab by rememberSaveable { mutableStateOf(AppTab.HOME) }
    var showPlanner by rememberSaveable { mutableStateOf(false) }
    var showPortal by rememberSaveable { mutableStateOf(false) }
    val passengers by viewModel.passengers.collectAsStateWithLifecycle()
    val bookings by viewModel.bookings.collectAsStateWithLifecycle()
    val trip by viewModel.trip.collectAsStateWithLifecycle()

    if (showPortal) {
        PortalScreen(
            trip = trip,
            passengers = passengers.filter { it.id in trip.passengerIds },
            onBack = { showPortal = false },
            onBookingSaved = { viewModel.saveBooking(it) }
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                AppTab.values().forEach { destination ->
                    NavigationBarItem(
                        selected = tab == destination,
                        onClick = { tab = destination },
                        icon = { Icon(destination.icon(), contentDescription = destination.label) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (tab) {
                AppTab.HOME -> HomeScreen(
                    passengers = passengers,
                    bookings = bookings,
                    onBookNow = { showPlanner = true },
                    onPassengers = { tab = AppTab.PASSENGERS },
                    onBookings = { tab = AppTab.BOOKINGS },
                    onSettings = { tab = AppTab.SETTINGS }
                )
                AppTab.PASSENGERS -> PassengersScreen(passengers, viewModel)
                AppTab.BOOKINGS -> BookingsScreen(bookings, viewModel)
                AppTab.SETTINGS -> SettingsScreen(viewModel)
            }
        }
    }

    if (showPlanner) {
        TripPlannerSheet(
            initial = trip,
            passengers = passengers,
            onDismiss = { showPlanner = false },
            onContinue = { selectedTrip ->
                viewModel.updateTrip(selectedTrip)
                showPlanner = false
                showPortal = true
            }
        )
    }
}

private fun AppTab.icon() = when (this) {
    AppTab.HOME -> Icons.Filled.Home
    AppTab.PASSENGERS -> Icons.Filled.Person
    AppTab.BOOKINGS -> Icons.Filled.History
    AppTab.SETTINGS -> Icons.Filled.Settings
}

@Composable
private fun Header(eyebrow: String, title: String, action: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text("BLACKMARK", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(4.dp))
            Text(eyebrow.uppercase(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(title, style = MaterialTheme.typography.headlineSmall)
        }
        action?.invoke()
    }
}

@Composable
private fun HomeScreen(
    passengers: List<Passenger>,
    bookings: List<BookingRecord>,
    onBookNow: () -> Unit,
    onPassengers: () -> Unit,
    onBookings: () -> Unit,
    onSettings: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Header("Lakshadweep ship tickets", "Book with confidence")
        }
        item {
            Card(
                Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(Modifier.padding(22.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.DirectionsBoat, null, Modifier.size(26.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("OFFICIAL PORTAL ONLY", style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(Modifier.height(15.dp))
                    Text("Your details, ready when you are.", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(7.dp))
                    Text("Save passenger profiles once, then review every field on the official site before you book.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = onBookNow,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimaryContainer, contentColor = MaterialTheme.colorScheme.primaryContainer)
                    ) { Text("BOOK NOW", fontWeight = FontWeight.Bold) }
                }
            }
        }
        item {
            Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickAction("Passengers", passengers.size.toString(), Icons.Filled.Person, onPassengers, Modifier.weight(1f))
                QuickAction("Bookings", bookings.size.toString(), Icons.Filled.History, onBookings, Modifier.weight(1f))
            }
        }
        item {
            Card(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Security, null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Portal status", style = MaterialTheme.typography.titleMedium)
                        Text("lakshadweep.irctc.co.in", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    AssistChip(onClick = { uriHandler.openUri(OFFICIAL_PORTAL) }, label = { Text("OPEN") }, leadingIcon = { Icon(Icons.Filled.OpenInNew, null, Modifier.size(16.dp)) })
                }
            }
        }
        item {
            Text("Safety promise", Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.titleMedium)
            Text("CAPTCHA, OTP, payment and final submission always stay with you. No credentials are stored.", Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            OutlinedButton(onClick = onSettings, Modifier.padding(horizontal = 20.dp).fillMaxWidth()) { Icon(Icons.Filled.Settings, null); Spacer(Modifier.width(8.dp)); Text("SETTINGS & PRIVACY") }
        }
    }
}

@Composable
private fun QuickAction(label: String, count: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier) {
    Card(modifier.clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(10.dp))
            Text(count, style = MaterialTheme.typography.headlineSmall)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PassengersScreen(passengers: List<Passenger>, viewModel: AppViewModel) {
    var editing by remember { mutableStateOf<Passenger?>(null) }
    var showEditor by rememberSaveable { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Header("Secure local storage", "Passengers", { IconButton(onClick = { editing = null; showEditor = true }) { Icon(Icons.Filled.Add, "Add passenger") } }) }
        if (passengers.isEmpty()) item { EmptyState("No saved passengers", "Add a passenger to speed up future form filling.") }
        items(passengers, key = { it.id }) { passenger ->
            PassengerCard(passenger, onEdit = { editing = passenger; showEditor = true }, onDelete = { viewModel.deletePassenger(passenger.id) })
        }
    }
    if (showEditor) PassengerEditor(initial = editing, onDismiss = { showEditor = false }, onSave = { viewModel.savePassenger(it); showEditor = false })
}

@Composable
private fun PassengerCard(passenger: Passenger, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(17.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(passenger.name.ifBlank { "Unnamed passenger" }, style = MaterialTheme.typography.titleMedium)
                    Text(listOf(passenger.gender, passenger.dateOfBirth).filter { it.isNotBlank() }.joinToString("  •  ").ifBlank { "Profile incomplete" }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, "Edit") }
                IconButton(onClick = onDelete) { Icon(Icons.Filled.DeleteOutline, "Delete", tint = MaterialTheme.colorScheme.error) }
            }
            Spacer(Modifier.height(10.dp))
            Text("ID  ${passenger.idType.ifBlank { "Not set" }}  •  ${passenger.idNumber.ifBlank { "Not set" }}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyState(title: String, body: String) {
    Column(Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Filled.Bookmark, null, Modifier.size(42.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(14.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PassengerEditor(initial: Passenger?, onDismiss: () -> Unit, onSave: (Passenger) -> Unit) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var dob by remember { mutableStateOf(initial?.dateOfBirth.orEmpty()) }
    var gender by remember { mutableStateOf(initial?.gender.orEmpty()) }
    var mobile by remember { mutableStateOf(initial?.mobile.orEmpty()) }
    var email by remember { mutableStateOf(initial?.email.orEmpty()) }
    var address by remember { mutableStateOf(initial?.address.orEmpty()) }
    var idType by remember { mutableStateOf(initial?.idType.orEmpty()) }
    var idNumber by remember { mutableStateOf(initial?.idNumber.orEmpty()) }
    var nationality by remember { mutableStateOf(initial?.nationality ?: "Indian") }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 20.dp), contentPadding = PaddingValues(bottom = 30.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Text(if (initial == null) "Add passenger" else "Edit passenger", style = MaterialTheme.typography.headlineSmall); Text("Stored encrypted on this device.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item { FormField("Full name", name) { name = it } }
            item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { FormField("Date of birth", dob, Modifier.weight(1f)) { dob = it }; FormField("Gender", gender, Modifier.weight(1f)) { gender = it } } }
            item { FormField("Mobile number", mobile) { mobile = it } }
            item { FormField("Email", email) { email = it } }
            item { FormField("Address", address, singleLine = false) { address = it } }
            item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { FormField("ID type", idType, Modifier.weight(1f)) { idType = it }; FormField("ID number", idNumber, Modifier.weight(1f)) { idNumber = it } } }
            item { FormField("Nationality", nationality) { nationality = it } }
            item { Button(onClick = { if (name.isNotBlank()) onSave(Passenger(initial?.id ?: java.util.UUID.randomUUID().toString(), name.trim(), dob.trim(), gender.trim(), mobile.trim(), email.trim(), address.trim(), idType.trim(), idNumber.trim(), nationality.trim())) }, Modifier.fillMaxWidth().height(52.dp)) { Text("SAVE PASSENGER") } }
            item { TextButton(onClick = onDismiss, Modifier.fillMaxWidth()) { Text("CANCEL") } }
        }
    }
}

@Composable
private fun FormField(label: String, value: String, modifier: Modifier = Modifier, singleLine: Boolean = true, onValueChange: (String) -> Unit) {
    OutlinedTextField(value, onValueChange, label = { Text(label) }, modifier = modifier.fillMaxWidth(), singleLine = singleLine, minLines = if (singleLine) 1 else 3)
}

@Composable
private fun BookingsScreen(bookings: List<BookingRecord>, viewModel: AppViewModel) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = bookings.filter { it.route.contains(query, true) || it.reference.contains(query, true) || it.passengerNames.any { name -> name.contains(query, true) } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Header("Local record", "Booking history") }
        item { OutlinedTextField(query, { query = it }, Modifier.padding(horizontal = 20.dp).fillMaxWidth(), label = { Text("Search bookings") }, leadingIcon = { Icon(Icons.Filled.Search, null) }, singleLine = true) }
        if (filtered.isEmpty()) item { EmptyState("No booking records", "Confirmed details appear here only after the official portal confirms them.") }
        items(filtered, key = { it.id }) { record ->
            Card(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(17.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text(record.route.ifBlank { "Official booking" }, style = MaterialTheme.typography.titleMedium); Text(record.journeyDate, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        AssistChip(onClick = {}, label = { Text(record.status) })
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("${record.reference.ifBlank { "Reference pending" }}  •  ${record.amount.ifBlank { "Amount on official ticket" }}", style = MaterialTheme.typography.bodyMedium)
                    Text(record.passengerNames.joinToString(", ").ifBlank { "Passenger details unavailable" }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(onClick = { viewModel.deleteBooking(record.id) }) { Icon(Icons.Filled.DeleteOutline, null); Spacer(Modifier.width(4.dp)); Text("DELETE") } }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(viewModel: AppViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var showLegal by remember { mutableStateOf<String?>(null) }
    var confirmClear by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Header("Your device", "Settings") }
        item {
            Card(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Fingerprint, null); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text("Biometric app lock", style = MaterialTheme.typography.titleMedium); Text("Optional device-level protection", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Switch(checked = settings.biometricLockEnabled, onCheckedChange = { viewModel.updateSettings(settings.copy(biometricLockEnabled = it)) }) }
                    Divider()
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Lock, null); Spacer(Modifier.width(12.dp)); Column { Text("What is never stored", style = MaterialTheme.typography.titleMedium); Text("Passwords, OTPs, card details, CVV, UPI PINs, and payment tokens.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                }
            }
        }
        item { Text("Legal & support", Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.titleMedium) }
        item { SettingLink(Icons.Filled.Policy, "Privacy policy", "How local data is protected") { showLegal = "privacy" } }
        item { SettingLink(Icons.Filled.Info, "Terms & conditions", "Use the official portal and verify before booking") { showLegal = "terms" } }
        item { SettingLink(Icons.Filled.OpenInNew, "Open official portal", "lakshadweep.irctc.co.in") { uriHandler.openUri(OFFICIAL_PORTAL) } }
        item { Button(onClick = { confirmClear = true }, Modifier.padding(horizontal = 20.dp).fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = Color.White)) { Text("CLEAR ALL LOCAL DATA") } }
    }
    if (confirmClear) AlertDialog(onDismissRequest = { confirmClear = false }, title = { Text("Clear local data?") }, text = { Text("This removes saved passengers, settings and booking history from this device. It cannot be undone.") }, confirmButton = { TextButton(onClick = { viewModel.clearAllData(); confirmClear = false }) { Text("CLEAR") } }, dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("CANCEL") } })
    showLegal?.let { kind -> AlertDialog(onDismissRequest = { showLegal = null }, title = { Text(if (kind == "privacy") "Privacy policy" else "Terms & conditions") }, text = { Text(if (kind == "privacy") PRIVACY_TEXT else TERMS_TEXT) }, confirmButton = { TextButton(onClick = { showLegal = null }) { Text("DONE") } }) }
}


@Composable
private fun SettingLink(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(Modifier.padding(horizontal = 20.dp).fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = MaterialTheme.colorScheme.secondary); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Icon(Icons.Filled.OpenInNew, null, Modifier.size(18.dp)) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripPlannerSheet(initial: TripDraft, passengers: List<Passenger>, onDismiss: () -> Unit, onContinue: (TripDraft) -> Unit) {
    var from by remember { mutableStateOf(initial.from) }
    var to by remember { mutableStateOf(initial.to) }
    var date by remember { mutableStateOf(initial.journeyDate) }
    var category by remember { mutableStateOf(initial.category) }
    var selected by remember { mutableStateOf(initial.passengerIds.toSet()) }
    val canContinue = from.isNotBlank() && to.isNotBlank() && date.isNotBlank() && selected.isNotEmpty()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 20.dp), contentPadding = PaddingValues(bottom = 30.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Text("Plan your trip", style = MaterialTheme.typography.headlineSmall); Text("Trip details are used to guide the official booking flow. Schedules, fares and availability come from the portal.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { FormField("From", from, Modifier.weight(1f)) { from = it }; FormField("To", to, Modifier.weight(1f)) { to = it } } }
            item { FormField("Journey date (DD/MM/YYYY)", date) { date = it } }
            item { Text("Category", style = MaterialTheme.typography.titleMedium) }
            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Standard", "Premium").forEach { option -> FilterChip(selected = category == option, onClick = { category = option }, label = { Text(option) }) } } }
            item { Text("Passengers", style = MaterialTheme.typography.titleMedium) }
            if (passengers.isEmpty()) item { Text("Add a passenger profile first.", color = MaterialTheme.colorScheme.error) }
            items(passengers, key = { it.id }) { passenger -> FilterChip(selected = passenger.id in selected, onClick = { selected = if (passenger.id in selected) selected - passenger.id else selected + passenger.id }, label = { Text(passenger.name.ifBlank { "Unnamed" }) }, modifier = Modifier.fillMaxWidth()) }
            item { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.WarningAmber, null); Spacer(Modifier.width(10.dp)); Text("You will review and complete the booking on the official website. This app never submits security or payment steps.", style = MaterialTheme.typography.bodyMedium) } } }
            item { Button(onClick = { onContinue(TripDraft(from.trim(), to.trim(), date.trim(), selected.toList(), category)) }, enabled = canContinue, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("CONTINUE TO OFFICIAL BOOKING") } }
            item { TextButton(onClick = onDismiss, Modifier.fillMaxWidth()) { Text("CANCEL") } }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortalScreen(trip: TripDraft, passengers: List<Passenger>, onBack: () -> Unit, onBookingSaved: (BookingRecord) -> Unit) {
    val context = LocalContext.current
    var pageTitle by remember { mutableStateOf("Connecting to official portal") }
    var securityStep by remember { mutableStateOf<FormAssistant.SecurityStep?>(null) }
    var assistantMessage by remember { mutableStateOf<String?>(null) }
    var showReview by remember { mutableStateOf(true) }
    var showConfirmation by remember { mutableStateOf(false) }
    var officialUrl by remember { mutableStateOf(OFFICIAL_PORTAL) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val assistant = remember { FormAssistant({ securityStep = it }, { assistantMessage = it }) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBar(title = { Column { Text("OFFICIAL PORTAL", style = MaterialTheme.typography.labelLarge); Text(pageTitle, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium) } }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } }, actions = { IconButton(onClick = { webViewRef?.reload() }) { Icon(Icons.Filled.Refresh, "Reload") } })
        Card(Modifier.padding(horizontal = 12.dp, vertical = 6.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Security, null, tint = MaterialTheme.colorScheme.secondary); Spacer(Modifier.width(9.dp)); Text("Manual verification and payment only. Review before every next step.", style = MaterialTheme.typography.bodyMedium) } }
        AndroidView(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            factory = { ctx -> WebView(ctx).apply {
                webViewRef = this
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        val host = request.url.host.orEmpty().lowercase()
                        val allowed = host == "lakshadweep.irctc.co.in" || host.endsWith(".irctc.co.in")
                        if (!allowed) Toast.makeText(context, "Navigation stopped: non-official domain.", Toast.LENGTH_SHORT).show()
                        return !allowed
                    }
                    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: android.net.http.SslError) { handler.cancel() }
                    override fun onPageFinished(view: WebView, url: String) {
                        pageTitle = view.title.orEmpty().ifBlank { "Official portal" }
                        officialUrl = url
                        FormAssistant.install(view)
                        assistant.inspect(view)
                        view.evaluateJavascript("(function(){const t=(document.body?.innerText||'').toLowerCase(); return (t.includes('booking confirmed') || t.includes('booking successful') || t.includes('confirmation')) ? 'yes' : 'no';})()") { result -> if (result.replace("\\\"", "").trim() == "yes") showConfirmation = true }
                    }
                }
                loadUrl(OFFICIAL_PORTAL)
            } },
            update = { webViewRef = it }
        )
        Row(Modifier.fillMaxWidth().padding(12.dp).navigationBarsPadding(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { webViewRef?.let { assistant.fillPassenger(it, passengers.firstOrNull() ?: Passenger()) } }, enabled = passengers.isNotEmpty(), modifier = Modifier.weight(1f)) { Icon(Icons.Filled.Person, null); Spacer(Modifier.width(6.dp)); Text("ASSIST FIELDS") }
            Button(onClick = { showReview = true }, Modifier.weight(1f)) { Text("REVIEW TRIP") }
        }
    }

    if (showReview) AlertDialog(onDismissRequest = { showReview = false }, title = { Text("Review your booking") }, text = { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("${trip.from.ifBlank { "From" }}  →  ${trip.to.ifBlank { "To" }}", fontWeight = FontWeight.Bold); Text("Journey date: ${trip.journeyDate.ifBlank { "Not set" }}"); Text("Passengers: ${passengers.joinToString { it.name.ifBlank { "Unnamed" } }.ifBlank { "None selected" }}"); Text("Category: ${trip.category}"); Spacer(Modifier.height(6.dp)); Text("Please verify all passenger, journey and fare details on the official Lakshadweep IRCTC website. The app will not submit the final booking without your confirmation.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }, confirmButton = { TextButton(onClick = { showReview = false }) { Text("CONTINUE") } }, dismissButton = { TextButton(onClick = onBack) { Text("EDIT") } })
    securityStep?.let { step ->
        AlertDialog(onDismissRequest = { securityStep = null }, title = { Text(if (step == FormAssistant.SecurityStep.CAPTCHA) "CAPTCHA required" else "OTP verification required") }, text = { Text(if (step == FormAssistant.SecurityStep.CAPTCHA) "Please complete the verification manually on the official website. Automation is paused." else "Enter the OTP manually on the official website. The app never reads, guesses or stores OTPs.") }, confirmButton = { TextButton(onClick = { securityStep = null }) { Text("I’LL DO THIS MANUALLY") } })
    }
    assistantMessage?.let { message ->
        LaunchedEffect(message) { Toast.makeText(context, message, Toast.LENGTH_LONG).show(); assistantMessage = null }
    }
    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("Official confirmation detected") },
            text = { Text("The official portal appears to show a confirmation page. Review the live page, then optionally save this record locally. LAK SHIP BOOK does not generate or replace the official ticket.") },
            confirmButton = {
                TextButton(onClick = {
                    onBookingSaved(BookingRecord(reference = "See official confirmation", bookingDate = today(), route = "${trip.from} → ${trip.to}", journeyDate = trip.journeyDate, passengerNames = passengers.map { it.name }, amount = "See official ticket", status = "Confirmed on official portal", officialUrl = officialUrl))
                    showConfirmation = false
                    Toast.makeText(context, "Saved. Use the official page to view the ticket.", Toast.LENGTH_LONG).show()
                }) { Text("SAVE RECORD") }
            },
            dismissButton = { Row { TextButton(onClick = { showConfirmation = false; webViewRef?.loadUrl(officialUrl) }) { Text("VIEW OFFICIAL TICKET") }; TextButton(onClick = { showConfirmation = false }) { Text("NOT NOW") } } }
        )
    }
}

private fun today(): String = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

private const val OFFICIAL_PORTAL = "https://lakshadweep.irctc.co.in/"
private const val PRIVACY_TEXT = "Passenger profiles and booking history are stored locally in encrypted Android storage. This app does not store passwords, OTPs, card numbers, CVV, UPI PINs, payment tokens or browsing history. The official portal handles authentication, availability, fares and payment. You can clear local data at any time."
private const val TERMS_TEXT = "LAK SHIP BOOK is a form-assistance utility for the official Lakshadweep IRCTC portal. You are responsible for reviewing passenger, journey and fare information and for completing CAPTCHA, OTP, payment and final confirmation yourself. This app does not guarantee availability or booking success and never treats an uncertain payment result as a successful booking."
