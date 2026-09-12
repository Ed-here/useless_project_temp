package com.example.phonedialer.data

import android.net.Uri

data class Contact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val photoUri: Uri? = null,
    val isFavorite: Boolean = false,
    val email: String? = null
)

enum class CallType {
    INCOMING, OUTGOING, MISSED
}

data class CallLogEntry(
    val id: String,
    val number: String,
    val name: String?,
    val type: CallType,
    val timestamp: Long,
    val durationSeconds: Long
)

enum class CallState {
    IDLE, RINGING, DIALING, ACTIVE, ON_HOLD, DISCONNECTED
}

data class ActiveCallInfo(
    val number: String = "",
    val name: String = "Unknown",
    val photoUri: Uri? = null,
    val state: CallState = CallState.IDLE,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isOnHold: Boolean = false,
    val durationSeconds: Long = 0
)
