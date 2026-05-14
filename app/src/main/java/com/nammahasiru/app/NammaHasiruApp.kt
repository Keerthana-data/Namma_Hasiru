package com.nammahasiru.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.nammahasiru.app.data.TreeRepository

class NammaHasiruApp : Application() {

    lateinit var treeRepository: TreeRepository
        private set

    override fun onCreate() {
        super.onCreate()
        treeRepository = TreeRepository(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            com.nammahasiru.app.reminders.CheckupReminderScheduler.CHANNEL_ID,
            getString(R.string.notification_channel),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }
}
