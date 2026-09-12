package com.example.phonedialer.util

import com.example.phonedialer.data.Contact

object T9SearchEngine {
    private val t9Map = mapOf(
        'a' to '2', 'b' to '2', 'c' to '2',
        'd' to '3', 'e' to '3', 'f' to '3',
        'g' to '4', 'h' to '4', 'i' to '4',
        'j' to '5', 'k' to '5', 'l' to '5',
        'm' to '6', 'n' to '6', 'o' to '6',
        'p' to '7', 'q' to '7', 'r' to '7', 's' to '7',
        't' to '8', 'u' to '8', 'v' to '8',
        'w' to '9', 'x' to '9', 'y' to '9', 'z' to '9'
    )

    fun filterContacts(queryDigits: String, contacts: List<Contact>): List<Contact> {
        if (queryDigits.isBlank()) return emptyList()

        val cleanQuery = queryDigits.replace("[^0-9]".toRegex(), "")
        if (cleanQuery.isEmpty()) return emptyList()

        return contacts.filter { contact ->
            // Check if phone number contains query digits
            val cleanPhone = contact.phoneNumber.replace("[^0-9]".toRegex(), "")
            if (cleanPhone.contains(cleanQuery)) return@filter true

            // Convert contact name to T9 digits
            val nameT9 = contact.name.lowercase().map { ch ->
                t9Map[ch] ?: ch
            }.joinToString("")

            nameT9.contains(cleanQuery)
        }
    }
}
