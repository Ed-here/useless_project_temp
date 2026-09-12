package com.example.phonedialer.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

class ContactsRepository(private val context: Context) {

    fun getContacts(): List<Contact> {
        val contactsList = mutableListOf<Contact>()
        val contentResolver: ContentResolver = context.contentResolver

        try {
            val cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_URI
                ),
                null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )

            cursor?.use {
                val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

                while (it.moveToNext()) {
                    val id = if (idIndex != -1) it.getString(idIndex) else ""
                    val name = if (nameIndex != -1) it.getString(nameIndex) ?: "Unknown" else "Unknown"
                    val number = if (numberIndex != -1) it.getString(numberIndex) ?: "" else ""
                    val photoStr = if (photoIndex != -1) it.getString(photoIndex) else null
                    val photoUri = photoStr?.let { uriStr -> Uri.parse(uriStr) }

                    if (number.isNotBlank()) {
                        contactsList.add(Contact(id = id, name = name, phoneNumber = number, photoUri = photoUri))
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Return real contacts if available, otherwise return seed data
        return if (contactsList.isNotEmpty()) contactsList else getSeedContacts()
    }

    private fun getSeedContacts(): List<Contact> {
        return listOf(
            Contact("1", "Alice Vance", "+1 (555) 019-2831", isFavorite = true, email = "alice.vance@example.com"),
            Contact("2", "Bob Smith", "+1 (555) 014-9823", isFavorite = true, email = "bob.smith@example.com"),
            Contact("3", "Charlie Davis", "+1 (555) 017-4412", isFavorite = false, email = "charlie.d@example.com"),
            Contact("4", "Diana Prince", "+1 (555) 018-9900", isFavorite = true, email = "diana@amazon.org"),
            Contact("5", "Ethan Hunt", "+1 (555) 012-3344", isFavorite = false, email = "ethan.hunt@imf.gov"),
            Contact("6", "Fiona Gallagher", "+1 (555) 016-7788", isFavorite = false, email = "fiona.g@example.com"),
            Contact("7", "George Miller", "+1 (555) 013-6655", isFavorite = false, email = "george.m@example.com"),
            Contact("8", "Hannah Abbott", "+1 (555) 015-1122", isFavorite = true, email = "hannah.a@example.com")
        )
    }
}
