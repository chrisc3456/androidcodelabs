package com.examples.jobscheduler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat

private const val PRIMARY_CHANNEL_ID = "primary_notification_channel"

class NotificationJobService: JobService() {

    private lateinit var notificationManager: NotificationManager

    /**
     * When the job starts with the specified parameters, set up the channel to handle notifications and build the required notification
     */
    override fun onStartJob(params: JobParameters?): Boolean {
        createNotificationChannel()
        val intent = Intent(this, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
            .setContentTitle("Job Service")
            .setContentText("Job completed")
            .setContentIntent(contentPendingIntent)
            .setSmallIcon(R.drawable.ic_job_running)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)

        notificationManager.notify(0, builder.build())

        // Returning false indicates that we don't want to keep the job running
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    /**
     * Set up the notification channel for the manager with additional configuration applied for supported OS versions for things like lights and vibration
     */
    private fun createNotificationChannel() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(PRIMARY_CHANNEL_ID, "Job Service Notification", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.apply {
                enableLights(true)
                enableVibration(true)
                lightColor = Color.RED
                description = "Notifications from Job Service"
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}