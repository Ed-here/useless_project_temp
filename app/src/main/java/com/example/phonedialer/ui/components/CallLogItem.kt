package com.example.phonedialer.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phonedialer.data.CallLogEntry
import com.example.phonedialer.data.CallType
import com.example.phonedialer.ui.theme.AccentGreen
import com.example.phonedialer.ui.theme.AccentRed
import com.example.phonedialer.ui.theme.DarkSurface
import com.example.phonedialer.ui.theme.PrimaryBlue
import com.example.phonedialer.ui.theme.TextPrimary
import com.example.phonedialer.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CallLogItem(
    log: CallLogEntry,
    onCallClick: (CallLogEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, color) = when (log.type) {
        CallType.MISSED -> Icons.AutoMirrored.Filled.CallMissed to AccentRed
        CallType.INCOMING -> Icons.AutoMirrored.Filled.CallReceived to AccentGreen
        CallType.OUTGOING -> Icons.AutoMirrored.Filled.CallMade to PrimaryBlue
    }

    val formattedTime = SimpleDateFormat("h:mm a, MMM d", Locale.getDefault()).format(Date(log.timestamp))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .clickable { onCallClick(log) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.name ?: log.number,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = if (log.type == CallType.MISSED) AccentRed else TextPrimary
            )
            Text(
                text = "$formattedTime • ${if (log.durationSeconds > 0) "${log.durationSeconds}s" else "No answer"}",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        val context = LocalContext.current
        val message = "YOUR LIFE WAS A USELESS PROJECT BY UR PARENTS"

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Real Call (First, Green)
            IconButton(
                onClick = { onCallClick(log) },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AccentGreen.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Callback",
                    tint = AccentGreen,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Fake Call 1 (Middle, Red)
            IconButton(
                onClick = { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() },
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(AccentRed.copy(alpha = 0.1f))
            ) {
                Icon(Icons.Default.Call, contentDescription = null, tint = AccentRed, modifier = Modifier.size(14.dp))
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Fake Call 2 (Last, Green) + Label
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() },
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(AccentGreen.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(14.dp))
                }
                Text("call", fontSize = 10.sp, color = TextSecondary)
            }
        }
    }
}
