package org.jphsystems.notes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jphsystems.notes.data.Note
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import kotlin.math.roundToInt

@Composable
fun DraggableNote(
    note: Note,
    onPositionChanged: (Float, Float) -> Unit,
    onContentChanged: (String) -> Unit,
    onColorChanged: (Long) -> Unit,
    onDelete: () -> Unit,
    isFocused: Boolean = false,
    onFocusChanged: (Boolean) -> Unit = {},
    darkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(note.x) }
    var offsetY by remember { mutableStateOf(note.y) }
    var isDragging by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val noteBackgroundColor = Color(note.color).copy(alpha = 0.9f)
    
    LaunchedEffect(isFocused) {
        if (isFocused) {
            delay(100)
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(isDragging) {
        if (isDragging) {
            focusManager.clearFocus()
        }
    }

    // Calculate base size based on content length
    val baseWidth = if (note.content.length > 200) 350.dp 
                   else if (note.content.length > 100) 300.dp 
                   else if (note.content.length > 50) 250.dp 
                   else 200.dp
    
    val baseHeight = if (note.content.length > 300) 280.dp
                    else if (note.content.length > 200) 240.dp 
                    else if (note.content.length > 100) 180.dp 
                    else if (note.content.length > 50) 140.dp 
                    else 100.dp
    
    val noteSize by animateDpAsState(
        targetValue = if (isEditing) baseWidth + 50.dp else baseWidth,
        animationSpec = spring()
    )

    val noteHeight by animateDpAsState(
        targetValue = if (isEditing) baseHeight + 40.dp else baseHeight,
        animationSpec = spring()
    )

    if (showColorPicker) {
        ColorPickerDialog(
            onColorSelected = onColorChanged,
            onDismiss = { showColorPicker = false }
        )
    }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .width(noteSize)
            .height(noteHeight)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { 
                if (!isDragging) {
                    focusRequester.requestFocus()
                }
            }
    ) {
        // Header for dragging
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(
                    color = Color(note.color).copy(alpha = 0.95f),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { 
                            isDragging = true
                            focusManager.clearFocus()
                        },
                        onDragEnd = {
                            isDragging = false
                            onPositionChanged(offsetX, offsetY)
                        },
                        onDragCancel = { 
                            isDragging = false
                            offsetX = note.x
                            offsetY = note.y
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    )
                }
        )

        // Main note content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = 24.dp)
                .background(
                    color = noteBackgroundColor,
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                )
                .padding(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { 
                        showColorPicker = true
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Change color",
                        tint = if (darkTheme) Color.Black.copy(alpha = 0.87f) else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(
                    onClick = { 
                        focusManager.clearFocus()
                        onDelete()
                    },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete note",
                        tint = if (darkTheme) Color.Black.copy(alpha = 0.87f) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                BasicTextField(
                    value = note.content,
                    onValueChange = onContentChanged,
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequester)
                        .onFocusChanged { 
                            isEditing = it.isFocused
                            onFocusChanged(it.isFocused)
                        },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = if (darkTheme) Color.Black.copy(alpha = 0.87f) else MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(if (darkTheme) Color.Black.copy(alpha = 0.87f) else MaterialTheme.colorScheme.onSurface)
                )
            }
        }
    }
}
