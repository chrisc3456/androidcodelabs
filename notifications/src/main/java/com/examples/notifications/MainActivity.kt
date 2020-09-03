package com.examples.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*

private const val PRIMARY_CHANNEL_ID: String = "primary_notification_channel"
private const val NOTIFICATION_ID: Int = 0
private const val ACTION_UPDATE_NOTIFICATION = "com.examples.notifications.ACTION_UPDATE_NOTIFICATION"  //prefix with app package name to ensure uniqueness
private const val ACTION_CANCEL_NOTIFICATION = "com.examples.notifications.ACTION_CANCEL_NOTIFICATION"

/**
 * Google Codelabs - Notifications
 * https://codelabs.developers.google.com/codelabs/android-training-notifications/
 */

class MainActivity : AppCompatActivity() {

    private lateinit var notificationManager: NotificationManager
    private val receiver = NotificationReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_create_notification.setOnClickListener { createNotification() }
        button_update_notification.setOnClickListener { updateNotification() }
        button_cancel_notification.setOnClickListener { cancelNotification() }

        // Set up the notification manager to handle new notifications
        createNotificationChannel()

        // Set up the receiver so it is notified when relevant intent actions are triggered
        registerReceiver(receiver, IntentFilter(ACTION_UPDATE_NOTIFICATION))
        registerReceiver(receiver, IntentFilter(ACTION_CANCEL_NOTIFICATION))

        setNotificationButtonState(isCreateEnabled = true, isUpdateEnabled = false, isCancelEnabled = false)
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    private fun setNotificationButtonState(isCreateEnabled: Boolean, isUpdateEnabled: Boolean, isCancelEnabled: Boolean) {
        button_cancel_notification.isEnabled = isCancelEnabled
        button_update_notification.isEnabled = isUpdateEnabled
        button_create_notification.isEnabled = isCreateEnabled
    }

    /**
     * Sets up a notification manager for the app ready to handle new notifications
     * Additionally a notification channel is created for devices on API 26+ to configure things like device lights and vibration settings when notifications are triggered via the manager
     */
    private fun createNotificationChannel() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Notification channels only available in API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(PRIMARY_CHANNEL_ID, "Example Notification", NotificationManager.IMPORTANCE_HIGH).apply {
                enableLights(true)
                enableVibration(true)
                lightColor = Color.RED
                description = "Notification from example app"
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    /**
     * Notifies the manager to trigger a new notification as defined when the builder is configured in getNotificationBuilder
     * Also applies a broadcast intent to the notification for an update action
     */
    private fun createNotification() {
        val updateIntent = Intent(ACTION_UPDATE_NOTIFICATION)
        val updatePendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_ID, updateIntent, PendingIntent.FLAG_ONE_SHOT)

        val builder = getNotificationBuilder()
        builder.addAction(R.drawable.ic_update, "Update Notification", updatePendingIntent)     // Icons specified in addAction are not displayed from Android Nougat onwards but still required for older devices

        notificationManager.notify(NOTIFICATION_ID, builder.build())
        setNotificationButtonState(isCreateEnabled = false, isUpdateEnabled = true, isCancelEnabled = true)
    }

    /**
     * Notifies the manager to update the existing notification with an additional style
     */
    private fun updateNotification() {
        val image = BitmapFactory.decodeResource(resources, R.drawable.mascot_1)
        val builder = getNotificationBuilder()
        builder.setStyle(
            NotificationCompat.BigPictureStyle()
                .bigPicture(image)
                .setBigContentTitle("Notification updated!")
        )
        notificationManager.notify(NOTIFICATION_ID, builder.build())
        setNotificationButtonState(isCreateEnabled = false, isUpdateEnabled = false, isCancelEnabled = true)
    }

    /**
     * Notifies the manager to cancel the existing notification
     */
    private fun cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
        setNotificationButtonState(isCreateEnabled = true, isUpdateEnabled = false, isCancelEnabled = false)
    }

    /**
     * Define the configuration of the notification to be triggered by this app
     * Includes an activity intent to open the app when the notification is tapped, along with an action intent fired when the notification is cancelled by the user
     */
    private fun getNotificationBuilder(): NotificationCompat.Builder {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val cancelledIntent = Intent(ACTION_CANCEL_NOTIFICATION)
        val pendingCancelledIntent = PendingIntent.getBroadcast(this, NOTIFICATION_ID, cancelledIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
            .setContentTitle("You've been notified")
            .setContentText("This is the notification text")
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentIntent(pendingIntent)                // Triggers the opening of the activity linked to the intent when tapped
            .setDeleteIntent(pendingCancelledIntent)        // Triggers an update of the button state to indicate the notification has been cancelled
            .setAutoCancel(true)                            // Closes the notification popup when tapped
            .setPriority(NotificationCompat.PRIORITY_HIGH)  // Provides backwards compatibility for Android 7.1 or lower, above this is done via notification channels
            .setDefaults(NotificationCompat.DEFAULT_ALL)    // Provides backwards compatibility for Android 7.1 or lower, above this is done via notification channels
    }

    /**
     * Broadcast receiver implementation to handle triggered intents
     */
    private inner class NotificationReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_UPDATE_NOTIFICATION -> updateNotification()
                ACTION_CANCEL_NOTIFICATION -> setNotificationButtonState(isCreateEnabled = true, isUpdateEnabled = false, isCancelEnabled = false)
            }
        }
    }
}
