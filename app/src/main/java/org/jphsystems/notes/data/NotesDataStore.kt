package org.jphsystems.notes.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "notes")

class NotesDataStore(private val context: Context) {
    private val notesKey = stringPreferencesKey("notes")

    // Expose the DataStore instance
    val dataStore: DataStore<Preferences> = context.dataStore

    suspend fun saveNotes(notes: List<Note>) {
        context.dataStore.edit { preferences ->
            preferences[notesKey] = Json.encodeToString(notes)
        }
    }

    val notes: Flow<List<Note>> = context.dataStore.data.map { preferences ->
        preferences[notesKey]?.let {
            try {
                Json.decodeFromString<List<Note>>(it)
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()
    }
}
