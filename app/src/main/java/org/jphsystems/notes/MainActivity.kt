package org.jphsystems.notes

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import org.jphsystems.notes.ui.BackgroundColorPicker
import org.jphsystems.notes.ui.BackgroundSettingsDialog
import org.jphsystems.notes.ui.DraggableNote
import org.jphsystems.notes.ui.NotesViewModel
import org.jphsystems.notes.ui.theme.NotesTheme
import org.jphsystems.notes.ui.Background

class MainActivity : ComponentActivity() {
    private val viewModel: NotesViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, user can select images
        } else {
            // Permission denied, show a message to the user
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotesTheme {
                NotesScreen(
                    viewModel = viewModel,
                    onImagePickerRequest = { checkAndRequestPermissions() }
                )
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}

@Composable
fun AboutScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = modifier,
            color = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Post-It Notes",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "A simple notes app with a fun UI",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    onImagePickerRequest: () -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val background by viewModel.background.collectAsState()
    var showBackgroundSettings by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    if (showBackgroundSettings) {
        BackgroundSettingsDialog(
            onColorPickerRequested = { showColorPicker = true },
            onImageSelected = { uri -> 
                viewModel.updateBackgroundImage(uri)
            },
            onDismiss = { showBackgroundSettings = false },
            onImagePickerRequest = onImagePickerRequest
        )
    }

    if (showColorPicker) {
        BackgroundColorPicker(
            onColorSelected = { color -> viewModel.updateBackgroundColor(color) },
            onDismiss = { showColorPicker = false }
        )
    }

    if (showAbout) {
        AboutScreen(
            onDismiss = { showAbout = false },
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }

    Scaffold(
        containerColor = Color.Yellow,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Post-It",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { showAbout = true }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "About"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Yellow
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.addNote() },
                containerColor = Color.Yellow
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add note",
                    tint = Color.Black,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    when (val bg = background) {
                        is Background.Color -> Modifier.background(Color(bg.color))
                        is Background.Image -> Modifier.paint(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(LocalContext.current)
                                    .data(bg.uri)
                                    .build()
                            ),
                            contentScale = ContentScale.Crop
                        )
                        else -> Modifier.background(Color.White)
                    }
                )
                .padding(top = 0.dp)  // Remove top padding to prevent double padding with TopAppBar
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { showBackgroundSettings = true }
                    )
                }
        ) {
            notes.forEach { note ->
                DraggableNote(
                    note = note,
                    onPositionChanged = { x, y ->
                        viewModel.updateNotePosition(note.id, x, y)
                    },
                    onContentChanged = { content ->
                        viewModel.updateNoteContent(note.id, content)
                    },
                    onColorChanged = { color ->
                        viewModel.updateNoteColor(note.id, color)
                    },
                    onDelete = {
                        viewModel.deleteNote(note.id)
                    }
                )
            }
        }
    }
}