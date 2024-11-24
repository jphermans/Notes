package org.jphsystems.notes

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    onImagePickerRequest: () -> Unit,
    darkTheme: Boolean,
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val background by viewModel.background.collectAsState()
    val focusedNoteId by viewModel.focusedNoteId.collectAsState()
    var showBackgroundSettings by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }

    if (showBackgroundSettings) {
        BackgroundSettingsDialog(
            onColorPickerRequested = { showColorPicker = true },
            onImageSelected = { uri ->
                viewModel.updateBackgroundImage(uri)
            },
            onDismiss = { showBackgroundSettings = false },
            onImagePickerRequest = onImagePickerRequest,
            darkTheme = darkTheme
        )
    }

    if (showColorPicker) {
        BackgroundColorPicker(
            onColorSelected = { color -> 
                viewModel.updateBackgroundColor(color)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    if (showAbout) {
        Dialog(
            onDismissRequest = { showAbout = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFEB3B)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "About Post-It Notes",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "A simple sticky notes app for quick\n reminders and thoughts.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Created by JPHsystems",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = { showAbout = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text("Close", color = Color.Black)
                    }
                }
            }
        }
    }

    if (showAddNoteDialog) {
        var noteText by remember { mutableStateOf("") }
        Dialog(
            onDismissRequest = { showAddNoteDialog = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFEB3B)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "Add Note",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter note text") },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        TextButton(
                            onClick = { showAddNoteDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = Color.Black)
                        }
                        TextButton(
                            onClick = {
                                if (noteText.isNotBlank()) {
                                    viewModel.addNote(noteText)
                                    noteText = ""
                                    showAddNoteDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Add", color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Post-It Notes",
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { showAbout = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    var showThemeMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { showThemeMenu = true }
                        ) {
                            Icon(
                                imageVector = when (themeMode) {
                                    ThemeMode.LIGHT -> Icons.Default.LightMode
                                    ThemeMode.DARK -> Icons.Default.DarkMode
                                    ThemeMode.SYSTEM -> if (darkTheme) Icons.Default.DarkMode else Icons.Default.LightMode
                                },
                                contentDescription = "Change theme",
                                tint = Color.Black
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { showThemeMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { 
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LightMode,
                                            contentDescription = null,
                                            tint = if (themeMode == ThemeMode.LIGHT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text("Light Theme")
                                    }
                                },
                                onClick = {
                                    onThemeModeChanged(ThemeMode.LIGHT)
                                    showThemeMenu = false
                                }
                            )
                            
                            DropdownMenuItem(
                                text = { 
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DarkMode,
                                            contentDescription = null,
                                            tint = if (themeMode == ThemeMode.DARK) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text("Dark Theme")
                                    }
                                },
                                onClick = {
                                    onThemeModeChanged(ThemeMode.DARK)
                                    showThemeMenu = false
                                }
                            )
                            
                            DropdownMenuItem(
                                text = { 
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (darkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                            contentDescription = null,
                                            tint = if (themeMode == ThemeMode.SYSTEM) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text("System Theme")
                                    }
                                },
                                onClick = {
                                    onThemeModeChanged(ThemeMode.SYSTEM)
                                    showThemeMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFEB3B)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddNoteDialog = true },
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
                    when {
                        darkTheme -> Modifier.background(MaterialTheme.colorScheme.background)
                        else -> when (val bg = background) {
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
                            else -> Modifier.background(MaterialTheme.colorScheme.background)
                        }
                    }
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { focusManager.clearFocus() }
                    )
                }
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { focusManager.clearFocus() },
                                    onLongPress = { showBackgroundSettings = true }
                                )
                            },
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 80.dp
                        )
                    ) {
                        items(notes) { note ->
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
                                },
                                darkTheme = darkTheme,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

class MainActivity : ComponentActivity() {
    private val viewModel: NotesViewModel by viewModels()
    private var themeMode by mutableStateOf(ThemeMode.SYSTEM)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startNotificationService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle intent when activity is first created
        handleIntent(intent)

        setContent {
            val isDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            
            NotesTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NotesScreen(
                        viewModel = viewModel,
                        onImagePickerRequest = { checkAndRequestPermissions() },
                        darkTheme = isDarkTheme,
                        themeMode = themeMode,
                        onThemeModeChanged = { newMode -> 
                            themeMode = newMode
                        }
                    )
                }
            }
        }

        checkNotificationPermission()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            "org.jphsystems.notes.CREATE_NOTE" -> {
                val noteText = intent.getStringExtra("note_text") ?: ""
                viewModel.addNote(noteText, autoFocus = true)
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