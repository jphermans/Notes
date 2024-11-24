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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
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
    // Set the background color of the note to the color stored in the Note object, 
    // but make sure it's fully opaque (alpha = 1.0f) so that the text is readable.
    val noteBackgroundColor = Color(note.color).copy(alpha = 1.0f)
    
    // Get the screen width to make notes responsive
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    // Fixed width at 70% of screen width
    val baseWidth = screenWidth * 0.7f
    
    val baseHeight by remember(note.content.length, screenWidth) {
        derivedStateOf {
            val maxHeight = minOf(screenWidth * 0.7f, 350.dp)
            when {
                note.content.length > 300 -> maxHeight * 0.9f
                note.content.length > 200 -> maxHeight * 0.8f
                note.content.length > 100 -> maxHeight * 0.7f
                note.content.length > 50 -> maxHeight * 0.6f
                else -> maxHeight * 0.5f
            }
        }
    }
    
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

    val noteSize by animateDpAsState(
        targetValue = if (isEditing) baseWidth + 100.dp else baseWidth,
        animationSpec = spring()
    )
    
    val noteHeight by animateDpAsState(
        targetValue = if (isEditing) baseHeight + 100.dp else baseHeight,
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
            .size(width = noteSize, height = noteHeight)
            .background(
                color = noteBackgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
    ) {
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
                            onPositionChanged(offsetX, offsetY)
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
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
                        imageVector = Icons.Default.Palette,
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
