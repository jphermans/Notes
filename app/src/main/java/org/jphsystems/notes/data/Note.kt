package org.jphsystems.notes.data

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: String,
    val content: String,
    val x: Float,
    val y: Float,
    val color: Long
)
