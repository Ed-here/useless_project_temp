package com.example.phonedialer

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.phonedialer.data.CallLogEntry
import com.example.phonedialer.data.CallLogRepository
import com.example.phonedialer.data.CallState
import com.example.phonedialer.data.Contact
import com.example.phonedialer.data.ContactsRepository
import com.example.phonedialer.data.SpeedDialManager
import com.example.phonedialer.service.CallManager
import com.example.phonedialer.ui.screens.ContactsScreen
import com.example.phonedialer.ui.screens.DialpadScreen
import com.example.phonedialer.ui.screens.InCallScreen
import com.example.phonedialer.ui.screens.IncomingCallScreen
import com.example.phonedialer.ui.screens.RecentsScreen
import com.example.phonedialer.ui.theme.AndroidPhoneDialerTheme
import com.example.phonedialer.ui.theme.DarkBackground
import com.example.phonedialer.ui.theme.DarkCard
import com.example.phonedialer.ui.theme.DarkSurface
import com.example.phonedialer.ui.theme.PrimaryBlue
import com.example.phonedialer.ui.theme.TextPrimary
import com.example.phonedialer.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class NavTab {
    KEYPAD, RECENTS, CONTACTS
}

enum class ConfirmationStep {
    NONE, SURE, REALLY_SURE, ELIGIBLE
}

class MainActivity : ComponentActivity() {

    private lateinit var contactsRepository: ContactsRepository
    private lateinit var callLogRepository: CallLogRepository
    private lateinit var speedDialManager: SpeedDialManager

    private var callToggle = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (!granted) {
            Toast.makeText(this, "Permissions needed for full phone functionality", Toast.LENGTH_SHORT).show()
        }
    }

    private val defaultDialerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (isDefaultDialer()) {
            Toast.makeText(this, "Custom Phone is now your default dialer!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        contactsRepository = ContactsRepository(this)
        callLogRepository = CallLogRepository(this)
        speedDialManager = SpeedDialManager(this)

        CallManager.init(this)

        requestRequiredPermissions()

        startPeriodicPrankTimer()

        setContent {
            AndroidPhoneDialerTheme {
                MainAppScreen()
            }
        }
    }

    private fun startPeriodicPrankTimer() {
        lifecycleScope.launch {
            // Confirmation that the timer has started
            Toast.makeText(this@MainActivity, "Prank service initialized.", Toast.LENGTH_SHORT).show()
            
            // First prank happens sooner (30s) to make verification easier
            delay(30000)
            
            while (true) {
                // Only trigger if no active call is happening
                if (CallManager.callState.value.state == CallState.IDLE) {
                    val contacts = contactsRepository.getContacts()
                    val randomContact = if (contacts.isNotEmpty()) contacts.random() else null
                    
                    val name = randomContact?.name ?: "Unknown Caller"
                    val number = randomContact?.phoneNumber ?: "000-000-0000"

                    CallManager.startSimulatedIncomingCall(
                        number = number,
                        name = name,
                        playAudioOnAnswer = true
                    )
                }
                
                // Subsequent pranks every 2 minutes
                delay(120000) 
            }
        }
    }

    private fun requestRequiredPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO
        )
        permissionLauncher.launch(permissions)
    }

    private fun isDefaultDialer(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
        } else {
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            packageName == telecomManager.defaultDialerPackage
        }
    }

    private fun requestDefaultDialerRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
            defaultDialerLauncher.launch(intent)
        } else {
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
            }
            defaultDialerLauncher.launch(intent)
        }
    }

    private fun initiatePhoneCall(number: String) {
        val contacts = contactsRepository.getContacts()
        
        val finalNumber: String
        val finalName: String

        if (contacts.isNotEmpty()) {
            val randomContact = contacts.random()
            finalNumber = randomContact.phoneNumber
            finalName = randomContact.name
        } else {
            finalNumber = number
            finalName = getContactName(number)
        }

        if (callToggle) {
            // Feature 1: Fake Call (Simulation + Audio)
            triggerFakeCallAfterDelay(finalNumber, finalName)
        } else {
            // Feature 2: Swapping calls feature (Random Contact)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                try {
                    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$finalNumber"))
                    startActivity(intent)
                } catch (e: Exception) {
                    CallManager.startSimulatedCall(finalNumber, finalName, playAudio = false)
                }
            } else {
                CallManager.startSimulatedCall(finalNumber, finalName, playAudio = false)
            }
        }
        
        // Alternate for the next call
        callToggle = !callToggle
    }

    private fun triggerFakeCallAfterDelay(number: String, name: String) {
        val randomDelay = Random.nextLong(1500, 3000)
        
        // Launch a coroutine to trigger the fake call
        lifecycleScope.launch {
            delay(randomDelay)
            CallManager.startSimulatedCall(
                number = number,
                name = name,
                playAudio = true
            )
        }
    }

    private fun getContactName(number: String): String {
        return contactsRepository.getContacts().find { it.phoneNumber.contains(number) }?.name ?: number
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainAppScreen() {
        var selectedTab by remember { mutableStateOf(NavTab.KEYPAD) }
        var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
        var callLogs by remember { mutableStateOf<List<CallLogEntry>>(emptyList()) }

        var confirmationStep by remember { mutableStateOf(ConfirmationStep.NONE) }
        var pendingNumber by remember { mutableStateOf("") }

        val activeCallInfo by CallManager.callState.collectAsState()

        LaunchedEffect(Unit) {
            contacts = contactsRepository.getContacts()
            callLogs = callLogRepository.getCallLogs()
        }

        // Confirmation Dialog Logic
        if (confirmationStep != ConfirmationStep.NONE) {
            AlertDialog(
                onDismissRequest = { confirmationStep = ConfirmationStep.NONE },
                title = {
                    val titleText = when (confirmationStep) {
                        ConfirmationStep.SURE -> "Are you sure?"
                        ConfirmationStep.REALLY_SURE -> "Are you REALLY sure?"
                        ConfirmationStep.ELIGIBLE -> "Are you eligible enough to make this call?"
                        else -> ""
                    }
                    Text(text = titleText, color = TextPrimary)
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            when (confirmationStep) {
                                ConfirmationStep.SURE -> confirmationStep = ConfirmationStep.REALLY_SURE
                                ConfirmationStep.REALLY_SURE -> {
                                    // Swapped: Clicking YES now cancels the call
                                    confirmationStep = ConfirmationStep.NONE
                                }
                                ConfirmationStep.ELIGIBLE -> {
                                    initiatePhoneCall(pendingNumber)
                                    confirmationStep = ConfirmationStep.NONE
                                }
                                else -> confirmationStep = ConfirmationStep.NONE
                            }
                        }
                    ) {
                        Text("YES", color = PrimaryBlue)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            if (confirmationStep == ConfirmationStep.REALLY_SURE) {
                                // Swapped: Clicking NO now proceeds to the next confirmation step
                                confirmationStep = ConfirmationStep.ELIGIBLE
                            } else {
                                confirmationStep = ConfirmationStep.NONE
                            }
                        }
                    ) {
                        Text("NO", color = TextSecondary)
                    }
                },
                containerColor = DarkSurface
            )
        }

        // Active / Incoming Call Fullscreen Overlays
        when (activeCallInfo.state) {
            CallState.RINGING -> {
                IncomingCallScreen(
                    callInfo = activeCallInfo,
                    onAnswerCall = { CallManager.answerCall() },
                    onRejectCall = { CallManager.rejectCall() },
                    onSendQuickSms = { msg ->
                        Toast.makeText(this@MainActivity, "SMS Sent: $msg", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            CallState.DIALING, CallState.ACTIVE, CallState.ON_HOLD -> {
                InCallScreen(
                    callInfo = activeCallInfo,
                    onMuteToggle = { CallManager.toggleMute() },
                    onSpeakerToggle = { CallManager.toggleSpeaker() },
                    onHoldToggle = { CallManager.toggleHold() },
                    onEndCall = { CallManager.endCall() }
                )
            }
            else -> {
                // Standard App Scaffold Layout
                Scaffold(
                    topBar = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkBackground)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Custom Phone",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            // Prompt to set Default Dialer if not set
                            if (!isDefaultDialer()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(PrimaryBlue.copy(alpha = 0.15f))
                                        .clickable { requestDefaultDialerRole() }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                    Text(
                                        text = "Tap to set as Default Dialer App",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PrimaryBlue
                                    )
                                }
                            }
                        }
                    },
                    bottomBar = {
                        NavigationBar(containerColor = DarkSurface) {
                            NavigationBarItem(
                                selected = selectedTab == NavTab.KEYPAD,
                                onClick = { selectedTab = NavTab.KEYPAD },
                                icon = { Icon(Icons.Default.Dialpad, contentDescription = "Keypad") },
                                label = { Text("Keypad") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryBlue,
                                    selectedTextColor = PrimaryBlue,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )
                            NavigationBarItem(
                                selected = selectedTab == NavTab.RECENTS,
                                onClick = { selectedTab = NavTab.RECENTS },
                                icon = { Icon(Icons.Default.History, contentDescription = "Recents") },
                                label = { Text("Recents") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryBlue,
                                    selectedTextColor = PrimaryBlue,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )
                            NavigationBarItem(
                                selected = selectedTab == NavTab.CONTACTS,
                                onClick = { selectedTab = NavTab.CONTACTS },
                                icon = { Icon(Icons.Default.People, contentDescription = "Contacts") },
                                label = { Text("Contacts") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryBlue,
                                    selectedTextColor = PrimaryBlue,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (selectedTab) {
                            NavTab.KEYPAD -> DialpadScreen(
                                contacts = contacts,
                                onInitiateCall = { number -> 
                                    pendingNumber = number
                                    confirmationStep = ConfirmationStep.SURE
                                },
                                onSpeedDialTriggered = { keyDigit ->
                                    speedDialManager.getSpeedDial(keyDigit)?.let { number ->
                                        pendingNumber = number
                                        confirmationStep = ConfirmationStep.SURE
                                    } ?: Toast.makeText(this@MainActivity, "No speed dial set for key $keyDigit", Toast.LENGTH_SHORT).show()
                                },
                                onAddContactClick = { number ->
                                    val intent = Intent(Intent.ACTION_INSERT).apply {
                                        type = android.provider.ContactsContract.Contacts.CONTENT_TYPE
                                        putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, number)
                                    }
                                    try {
                                        startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(this@MainActivity, "Could not launch Contacts editor", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                            NavTab.RECENTS -> RecentsScreen(
                                callLogs = callLogs,
                                onInitiateCall = { number -> 
                                    pendingNumber = number
                                    confirmationStep = ConfirmationStep.SURE
                                }
                            )
                            NavTab.CONTACTS -> ContactsScreen(
                                contacts = contacts,
                                onInitiateCall = { number -> 
                                    pendingNumber = number
                                    confirmationStep = ConfirmationStep.SURE
                                },
                                onSendMessage = { number ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$number"))
                                    startActivity(intent)
                                },
                                onAddContactClick = {
                                    val intent = Intent(Intent.ACTION_INSERT).apply {
                                        type = android.provider.ContactsContract.Contacts.CONTENT_TYPE
                                    }
                                    startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
