package org.jphsystems.notes.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import org.jphsystems.notes.R

class NotesWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            // Create RemoteViews
            val views = RemoteViews(context.packageName, R.layout.widget_notes).apply {
                // Set the hint text
                setTextViewText(R.id.widget_note_input, "Tap to create a post-it note...")

                // Create intent for QuickNoteActivity
                val intent = Intent(context, QuickNoteActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }

                val pendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Set click listener for both the text and button
                setOnClickPendingIntent(R.id.add_note_button, pendingIntent)
                setOnClickPendingIntent(R.id.widget_note_input, pendingIntent)
            }

            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // Clean up preferences when widget is deleted
        val prefs = context.getSharedPreferences(NotesWidgetConfigureActivity.PREFS_NAME, 0)
        prefs.edit().apply {
            appWidgetIds.forEach { appWidgetId ->
                remove(NotesWidgetConfigureActivity.PREF_PREFIX_KEY + appWidgetId)
            }
            apply()
        }
    }
}
