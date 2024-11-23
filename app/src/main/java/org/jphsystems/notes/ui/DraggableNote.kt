package org.jphsystems.notes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.jphsystems.notes.data.Note
import org.jphsystems.notes.ui.theme.NoteDarkBackground
import org.jphsystems.notes.ui.theme.NoteLightBackground
import kotlin.math.roundToInt

@Composable
fun DraggableNote(
    note: Note,
    onPositionChanged: (Float, Float) -> Unit,
    onContentChanged: (String) -> Unit,
    onColorChanged: (Long) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(note.x) }
    var offsetY by remember { mutableStateOf(note.y) }
    var isDragging by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val noteBackgroundColor = if (isSystemInDarkTheme) NoteDarkBackground else NoteLightBackground
    
    val transition = updateTransition(targetState = isEditing, label = "editing")
    val noteSize by transition.animateDp(
        transitionSpec = { spring(stiffness = Spring.StiffnessLow) },
        label = "size"
    ) { editing -> if (editing) 300.dp else 200.dp }

    if (showColorPicker) {
        ColorPickerDialog(
            onColorSelected = onColorChanged,
            onDismiss = { showColorPicker = false }
        )
    }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .background(
                color = Color(note.color).copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
            .width(noteSize)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = {
                        isDragging = false
                        onPositionChanged(offsetX, offsetY)
                    },
                    onDragCancel = { isDragging = false },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .size(noteSize)
                .background(
                    color = noteBackgroundColor,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditing) {
                    IconButton(
                        onClick = {
                            isEditing = false
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Done editing",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    IconButton(
                        onClick = { showColorPicker = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Change color",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete note",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            TextField(
                value = note.content,
                onValueChange = { if (!isDragging) onContentChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        isEditing = focusState.isFocused
                    },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}
