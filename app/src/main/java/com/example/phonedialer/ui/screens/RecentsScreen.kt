package com.example.phonedialer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phonedialer.data.CallLogEntry
import com.example.phonedialer.data.CallType
import com.example.phonedialer.ui.components.CallLogItem
import com.example.phonedialer.ui.theme.AccentRed
import com.example.phonedialer.ui.theme.DarkBackground
import com.example.phonedialer.ui.theme.DarkCard
import com.example.phonedialer.ui.theme.PrimaryBlue
import com.example.phonedialer.ui.theme.TextPrimary
import com.example.phonedialer.ui.theme.TextSecondary

@Composable
fun RecentsScreen(
    callLogs: List<CallLogEntry>,
    onInitiateCall: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMissedOnly by remember { mutableStateOf(false) }

    val filteredLogs = remember(showMissedOnly, callLogs) {
        if (showMissedOnly) callLogs.filter { it.type == CallType.MISSED }
        else callLogs
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Recents",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chips (All / Missed)
        Row(modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = !showMissedOnly,
                onClick = { showMissedOnly = false },
                label = { Text("All Calls", color = if (!showMissedOnly) Color.White else TextSecondary) },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    containerColor = DarkCard
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilterChip(
                selected = showMissedOnly,
                onClick = { showMissedOnly = true },
                label = { Text("Missed", color = if (showMissedOnly) Color.White else TextSecondary) },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentRed,
                    containerColor = DarkCard
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredLogs) { log ->
                CallLogItem(
                    log = log,
                    onCallClick = { onInitiateCall(log.number) }
                )
            }
        }
    }
}
