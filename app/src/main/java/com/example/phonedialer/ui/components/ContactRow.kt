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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phonedialer.data.Contact
import com.example.phonedialer.ui.theme.AccentGreen
import com.example.phonedialer.ui.theme.AccentRed
import com.example.phonedialer.ui.theme.DarkCard
import com.example.phonedialer.ui.theme.DarkSurface
import com.example.phonedialer.ui.theme.PrimaryBlue
import com.example.phonedialer.ui.theme.TextPrimary
import com.example.phonedialer.ui.theme.TextSecondary

@Composable
fun ContactRow(
    contact: Contact,
    onCallClick: (Contact) -> Unit,
    onContactClick: (Contact) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .clickable { onContactClick(contact) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar circle with contact initials
        val initial = contact.name.take(1).uppercase()
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(PrimaryBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = TextPrimary
            )
            Text(
                text = contact.phoneNumber,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }

        val context = LocalContext.current
        val message = "YOUR LIFE WAS A USELESS PROJECT BY UR PARENTS"

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Real Call (First, Green)
            IconButton(
                onClick = { onCallClick(contact) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AccentGreen.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    tint = AccentGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Fake Call 1 (Middle, Red)
            IconButton(
                onClick = { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() },
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AccentRed.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = AccentRed,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Fake Call 2 (Last, Green) + Label
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AccentGreen.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text("call", fontSize = 10.sp, color = TextSecondary)
            }
        }
    }
}
