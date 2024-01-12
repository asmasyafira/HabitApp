package com.dicoding.habitapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dicoding.habitapp.R
import com.dicoding.habitapp.ui.detail.DetailHabitActivity
import com.dicoding.habitapp.utils.HABIT_ID
import com.dicoding.habitapp.utils.HABIT_TITLE
import com.dicoding.habitapp.utils.NOTIFICATION_CHANNEL_ID

class NotificationWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private val habitId = inputData.getInt(HABIT_ID, 0)
    private val habitTitle = inputData.getString(HABIT_TITLE)

    override fun doWork(): Result {
        val prefManager = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val shouldNotify = prefManager.getBoolean(applicationContext.getString(R.string.pref_key_notify), false)

        //TODO 12 : If notification preference on, show notification with pending intent

        if (shouldNotify) {
            if (habitTitle != null) {
                val intent = Intent(applicationContext, DetailHabitActivity::class.java)
                intent.putExtra(HABIT_ID, habitId)
                val pendingIntent = TaskStackBuilder.create(applicationContext).run {
                    addNextIntentWithParentStack(intent)
                    getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
                }
                val notifManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notifBuilder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(habitTitle)
                    .setSmallIcon(R.drawable.ic_notifications)
                    .setContentText(applicationContext.getString(R.string.notify_content))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "channelName", NotificationManager.IMPORTANCE_DEFAULT)
                    channel.enableVibration(true)
                    channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)

                    notifBuilder.setChannelId(NOTIFICATION_CHANNEL_ID)
                    notifManager.createNotificationChannel(channel)
                }

                val notification = notifBuilder.build()
                notifManager.notify(100, notification)
            }
        }
        return Result.success()
    }

}
