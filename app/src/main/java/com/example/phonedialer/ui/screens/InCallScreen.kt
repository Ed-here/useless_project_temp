package com.example.phonedialer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phonedialer.data.ActiveCallInfo
import com.example.phonedialer.data.CallState
import com.example.phonedialer.ui.components.CallControlBtn
import com.example.phonedialer.ui.components.DialButton
import com.example.phonedialer.ui.theme.AccentGreen
import com.example.phonedialer.ui.theme.AccentRed
import com.example.phonedialer.ui.theme.DarkBackground
import com.example.phonedialer.ui.theme.DarkCard
import com.example.phonedialer.ui.theme.DarkSurface
import com.example.phonedialer.ui.theme.PrimaryBlue
import com.example.phonedialer.ui.theme.TextPrimary
import com.example.phonedialer.ui.theme.TextSecondary
import com.example.phonedialer.util.DtmfTonePlayer
import java.util.Locale

@Composable
fun InCallScreen(
    callInfo: ActiveCallInfo,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onHoldToggle: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showInCallKeypad by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }

    // Pulsing glowing animation for caller avatar ring
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val minutes = callInfo.durationSeconds / 60
    val seconds = callInfo.durationSeconds % 60
    val formattedDuration = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    val statusText = when (callInfo.state) {
        CallState.DIALING -> "Dialing..."
        CallState.ACTIVE -> formattedDuration
        CallState.ON_HOLD -> "On Hold ($formattedDuration)"
        CallState.DISCONNECTED -> "Call Ended"
        else -> ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Avatar Pulsing Ring
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(if (callInfo.state == CallState.ACTIVE) pulseScale else 1.0f)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.2f))
            )
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = callInfo.name.take(1).uppercase(),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = callInfo.name,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = callInfo.number,
            fontSize = 16.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Call Timer / Status pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = statusText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (callInfo.state == CallState.ON_HOLD) AccentRed else AccentGreen
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // In-Call Keypad Overlay Drawer
        AnimatedVisibility(visible = showInCallKeypad) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurface)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "In-Call Keypad",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val keypadKeys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("*", "0", "#")
                )

                keypadKeys.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { digit ->
                            DialButton(
                                digit = digit,
                                letters = "",
                                onClick = { DtmfTonePlayer.playTone(it.first()) },
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Active Call Controls (6-Button Grid)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CallControlBtn(
                icon = if (callInfo.isMuted) Icons.Default.Mic else Icons.Default.MicOff,
                label = if (callInfo.isMuted) "Mute" else "Unmute",
                isActive = !callInfo.isMuted,
                onClick = onMuteToggle
            )

            CallControlBtn(
                icon = Icons.Default.Dialpad,
                label = "Keypad",
                isActive = showInCallKeypad,
                onClick = { showInCallKeypad = !showInCallKeypad }
            )

            CallControlBtn(
                icon = Icons.Default.VolumeUp,
                label = "Speaker",
                isActive = callInfo.isSpeakerOn,
                onClick = onSpeakerToggle
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CallControlBtn(
                icon = Icons.Default.Pause,
                label = if (callInfo.isOnHold) "Unhold" else "Hold",
                isActive = callInfo.isOnHold,
                onClick = onHoldToggle
            )

            CallControlBtn(
                icon = Icons.Default.FiberManualRecord,
                label = if (isRecording) "Rec..." else "Record",
                isActive = isRecording,
                activeBg = AccentRed,
                onClick = { isRecording = !isRecording }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // End Call Button (Large Red)
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(AccentRed)
                .clickable { onEndCall() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CallEnd,
                contentDescription = "End Call",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
