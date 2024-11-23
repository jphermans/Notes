package org.jphsystems.notes.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import org.jphsystems.notes.MainActivity
import org.jphsystems.notes.R
import org.jphsystems.notes.ui.NotesViewModel

class NotificationService : Service() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var viewModel: NotesViewModel

    companion object {
        private const val CHANNEL_ID = "NotesQuickAccess"
        private const val NOTIFICATION_ID = 1
        private const val ACTION_CREATE_NOTE = "org.jphsystems.notes.CREATE_NOTE"
        private const val FOREGROUND_SERVICE_TYPE_SPECIAL_USE = ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE

        fun startService(context: Context) {
            val intent = Intent(context, NotificationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            .create(NotesViewModel::class.java)
        createNotificationChannel()
        observeNotes()
    }

    private fun observeNotes() {
        scope.launch {
            viewModel.notes.collectLatest { notes ->
                updateNotification(notes.size)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showPersistentNotification()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Post-it Quick Access",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Quick access to create new post-it notes"
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(noteCount: Int) {
        val notification = createNotification(noteCount)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showPersistentNotification() {
        val notification = createNotification(0)  // Initial count, will be updated by observer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotification(noteCount: Int): android.app.Notification {
        // Intent for opening the app
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent for creating a new note
        val createNoteIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_CREATE_NOTE
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val createNotePendingIntent = PendingIntent.getActivity(
            this, 1, createNoteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notesText = when (noteCount) {
            0 -> "No notes yet"
            1 -> "1 note"
            else -> "$noteCount notes in App"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Post-it")
            .setContentText(notesText)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                R.drawable.ic_add,
                "Add New Note",
                createNotePendingIntent
            )
            .build()
    }
}
