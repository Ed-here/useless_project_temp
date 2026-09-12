package com.example.phonedialer.data

import android.content.ContentResolver
import android.content.Context
import android.provider.CallLog

class CallLogRepository(private val context: Context) {

    fun getCallLogs(): List<CallLogEntry> {
        val logsList = mutableListOf<CallLogEntry>()
        val contentResolver: ContentResolver = context.contentResolver

        try {
            val cursor = contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(
                    CallLog.Calls._ID,
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DATE,
                    CallLog.Calls.DURATION
                ),
                null, null, CallLog.Calls.DATE + " DESC"
            )

            cursor?.use {
                val idIndex = it.getColumnIndex(CallLog.Calls._ID)
                val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
                val nameIndex = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
                val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
                val durationIndex = it.getColumnIndex(CallLog.Calls.DURATION)

                while (it.moveToNext()) {
                    val id = if (idIndex != -1) it.getString(idIndex) else ""
                    val number = if (numberIndex != -1) it.getString(numberIndex) ?: "Unknown" else "Unknown"
                    val name = if (nameIndex != -1) it.getString(nameIndex) else null
                    val rawType = if (typeIndex != -1) it.getInt(typeIndex) else CallLog.Calls.INCOMING_TYPE
                    val date = if (dateIndex != -1) it.getLong(dateIndex) else System.currentTimeMillis()
                    val duration = if (durationIndex != -1) it.getLong(durationIndex) else 0L

                    val callType = when (rawType) {
                        CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
                        CallLog.Calls.MISSED_TYPE -> CallType.MISSED
                        else -> CallType.INCOMING
                    }

                    logsList.add(
                        CallLogEntry(
                            id = id,
                            number = number,
                            name = name,
                            type = callType,
                            timestamp = date,
                            durationSeconds = duration
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return if (logsList.isNotEmpty()) logsList else getSeedCallLogs()
    }

    private fun getSeedCallLogs(): List<CallLogEntry> {
        val now = System.currentTimeMillis()
        val hourMs = 3600 * 1000L
        return listOf(
            CallLogEntry("101", "+1 (555) 019-2831", "Alice Vance", CallType.MISSED, now - (1 * hourMs), 0),
            CallLogEntry("102", "+1 (555) 014-9823", "Bob Smith", CallType.OUTGOING, now - (3 * hourMs), 142),
            CallLogEntry("103", "+1 (555) 018-9900", "Diana Prince", CallType.INCOMING, now - (5 * hourMs), 310),
            CallLogEntry("104", "+1 (555) 012-3344", "Ethan Hunt", CallType.MISSED, now - (12 * hourMs), 0),
            CallLogEntry("105", "+1 (555) 017-4412", "Charlie Davis", CallType.OUTGOING, now - (24 * hourMs), 45)
        )
    }
}
