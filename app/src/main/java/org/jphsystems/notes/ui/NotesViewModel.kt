package org.jphsystems.notes.ui

import android.app.Application
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jphsystems.notes.data.Note
import org.jphsystems.notes.data.NotesDataStore
import java.util.UUID
import kotlin.random.Random

sealed class Background {
    data class Color(val color: Long) : Background()
    data class Image(val uri: String) : Background()
}

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val notesDataStore = NotesDataStore(application)
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _background = MutableStateFlow<Background>(Background.Color(0xFFFFFFFF))
    val background: StateFlow<Background> = _background.asStateFlow()

    companion object {
        val noteColors = listOf(
            0xFFFFB3BA, // Light pink
            0xFFBAE1FF, // Light blue
            0xFFBAFFBA, // Light green
            0xFFFFE4BA, // Light orange
            0xFFE2BAFF, // Light purple
            0xFFFFFFBA, // Light yellow
            0xFFFFB3E6, // Light magenta
            0xFFB3FFE6  // Light cyan
        ).map { it.toLong() }

        val backgroundColors = listOf(
            0xFFFFFFFF, // White
            0xFFF0F0F0, // Light Gray
            0xFFFFF8DC, // Cornsilk
            0xFFF0FFFF, // Azure
            0xFFF5F5DC, // Beige
            0xFFFFFAF0, // Floral White
            0xFFF0FFF0, // Honeydew
            0xFFFFF0F5  // Lavender Blush
        ).map { it.toLong() }

        private val BACKGROUND_COLOR_KEY = longPreferencesKey("background_color")
        private val BACKGROUND_IMAGE_KEY = stringPreferencesKey("background_image")
        private val BACKGROUND_TYPE_KEY = stringPreferencesKey("background_type")
    }

    init {
        viewModelScope.launch {
            notesDataStore.notes.collect { notes ->
                _notes.value = notes
            }
        }
        viewModelScope.launch {
            notesDataStore.dataStore.data.map { preferences ->
                when (preferences[BACKGROUND_TYPE_KEY]) {
                    "image" -> preferences[BACKGROUND_IMAGE_KEY]?.let { Background.Image(it) }
                    else -> preferences[BACKGROUND_COLOR_KEY]?.let { Background.Color(it) }
                } ?: Background.Color(0xFFFFFFFF)
            }.collect { background ->
                _background.value = background
            }
        }
    }

    fun updateBackgroundColor(color: Long) {
        viewModelScope.launch {
            notesDataStore.dataStore.edit { preferences ->
                preferences[BACKGROUND_TYPE_KEY] = "color"
                preferences[BACKGROUND_COLOR_KEY] = color
            }
            _background.value = Background.Color(color)
        }
    }

    fun updateBackgroundImage(uri: Uri) {
        viewModelScope.launch {
            notesDataStore.dataStore.edit { preferences ->
                preferences[BACKGROUND_TYPE_KEY] = "image"
                preferences[BACKGROUND_IMAGE_KEY] = uri.toString()
            }
            _background.value = Background.Image(uri.toString())
        }
    }

    fun addNote(content: String = "New Note") {
        val newNote = Note(
            id = UUID.randomUUID().toString(),
            content = content,
            x = Random.nextFloat() * 300f,
            y = Random.nextFloat() * 300f,
            color = noteColors.random()
        )
        updateNotes(_notes.value + newNote)
    }

    fun updateNotePosition(id: String, x: Float, y: Float) {
        val updatedNotes = _notes.value.map { note ->
            if (note.id == id) note.copy(x = x, y = y) else note
        }
        updateNotes(updatedNotes)
    }

    fun updateNoteContent(id: String, content: String) {
        val updatedNotes = _notes.value.map { note ->
            if (note.id == id) note.copy(content = content) else note
        }
        updateNotes(updatedNotes)
    }

    fun updateNoteColor(id: String, color: Long) {
        val updatedNotes = _notes.value.map { note ->
            if (note.id == id) note.copy(color = color) else note
        }
        updateNotes(updatedNotes)
    }

    fun deleteNote(id: String) {
        updateNotes(_notes.value.filter { it.id != id })
    }

    private fun updateNotes(notes: List<Note>) {
        viewModelScope.launch {
            _notes.value = notes
            notesDataStore.saveNotes(notes)
        }
    }
}
