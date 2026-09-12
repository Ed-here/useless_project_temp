package com.example.phonedialer.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.phonedialer.data.Contact
import com.example.phonedialer.ui.components.ContactRow
import com.example.phonedialer.ui.components.DialButton
import com.example.phonedialer.ui.theme.AccentGreen
import com.example.phonedialer.ui.theme.DarkBackground
import com.example.phonedialer.ui.theme.DarkCard
import com.example.phonedialer.ui.theme.DarkSurface
import com.example.phonedialer.ui.theme.TextPrimary
import com.example.phonedialer.ui.theme.TextSecondary
import com.example.phonedialer.util.T9SearchEngine

@Composable
fun DialpadScreen(
    contacts: List<Contact>,
    onInitiateCall: (String) -> Unit,
    onSpeedDialTriggered: (Int) -> Unit,
    onAddContactClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputDigits by remember { mutableStateOf("") }
    val filteredContacts = remember(inputDigits, contacts) {
        if (inputDigits.isNotEmpty()) {
            T9SearchEngine.filterContacts(inputDigits, contacts)
        } else emptyList()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // T9 Search Match Preview List
        AnimatedVisibility(visible = filteredContacts.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .padding(8.dp)
            ) {
                LazyColumn {
                    items(filteredContacts) { contact ->
                        ContactRow(
                            contact = contact,
                            onCallClick = { onInitiateCall(it.phoneNumber) },
                            onContactClick = { inputDigits = it.phoneNumber }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Entered Digits Display Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (inputDigits.isEmpty()) "" else inputDigits,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1
            )
        }

        // Action Sub-bar (Add to Contacts option)
        if (inputDigits.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkCard)
                    .clickable { onAddContactClick(inputDigits) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Add Contact",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(
                    text = "Add to Contacts",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dial Keypad Grid (4 Rows)
        val keys = listOf(
            listOf("1" to "", "2" to "ABC", "3" to "DEF"),
            listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
            listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
            listOf("*" to "", "0" to "+", "#" to "")
        )

        keys.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { (digit, letters) ->
                    DialButton(
                        digit = digit,
                        letters = letters,
                        onClick = { char -> inputDigits += char },
                        onLongClick = { char ->
                            if (char == "0") {
                                inputDigits += "+"
                            } else {
                                char.toIntOrNull()?.let { digitInt ->
                                    onSpeedDialTriggered(digitInt)
                                }
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bottom Action Bar: Call Button & Backspace Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.size(56.dp)) // Spacer for alignment

            // Large Green Call Button
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(AccentGreen)
                    .clickable {
                        if (inputDigits.isNotEmpty()) {
                            onInitiateCall(inputDigits)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Backspace Button
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                if (inputDigits.isNotEmpty()) {
                    IconButton(
                        onClick = { inputDigits = inputDigits.dropLast(1) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Backspace",
                            tint = TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
