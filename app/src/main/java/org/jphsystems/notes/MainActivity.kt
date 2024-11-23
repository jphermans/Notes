package org.jphsystems.notes

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import org.jphsystems.notes.ui.Background
import org.jphsystems.notes.ui.BackgroundColorPicker
import org.jphsystems.notes.ui.BackgroundSettingsDialog
import org.jphsystems.notes.ui.DraggableNote
import org.jphsystems.notes.ui.NotesViewModel
import org.jphsystems.notes.ui.theme.NotesTheme

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    onImagePickerRequest: () -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val background by viewModel.background.collectAsState()
    val focusedNoteId by viewModel.focusedNoteId.collectAsState()
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
                        "Post-It Notes",
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
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                containerColor = Color(0xFFFFEB3B)  // Material Yellow
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
        val focusManager = LocalFocusManager.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .then(
                    when (val bg = background) {
                        is Background.Color -> Modifier.background(Color(bg.color))
                        is Background.Image -> Modifier.paint(
                            painter = rememberAsyncImagePainter(
                                ImageRequest
                                    .Builder(LocalContext.current)
                                    .data(bg.uri)
                                    .build()
                            ),
                            contentScale = ContentScale.Crop
                        )

                        else -> Modifier.background(Color.White)
                    }
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { focusManager.clearFocus() },
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
                    },
                    isFocused = note.id == focusedNoteId,
                    onFocusChanged = { isFocused ->
                        if (!isFocused) {
                            viewModel.clearNoteFocus()
                        }
                    }
                )
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    private val viewModel: NotesViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startNotificationService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set status bar color to yellow
        window.statusBarColor = Color(0xFFFFEB3B).toArgb()
        // Make status bar icons dark for better visibility on yellow background
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        checkNotificationPermission()
        handleIntent(intent)

        setContent {
            NotesTheme {
                NotesScreen(
                    viewModel = viewModel,
                    onImagePickerRequest = { checkAndRequestPermissions() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            "org.jphsystems.notes.CREATE_NOTE" -> {
                viewModel.addNote("New Quick Note", autoFocus = true)
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                startNotificationService()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            startNotificationService()
        }
    }

    private fun startNotificationService() {
        org.jphsystems.notes.service.NotificationService.startService(this)
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}