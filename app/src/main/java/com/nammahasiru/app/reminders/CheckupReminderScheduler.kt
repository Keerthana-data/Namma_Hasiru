package com.nammahasiru.app.reminders

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object CheckupReminderScheduler {

    const val CHANNEL_ID = "namma_hasiru_checkup"

    private fun workName(treeId: Long) = "tree_checkup_$treeId"

    fun scheduleForNewTree(context: Context, treeId: Long) {
        val input = Data.Builder().putLong(CheckupReminderWorker.KEY_TREE_ID, treeId).build()
        val request = OneTimeWorkRequestBuilder<CheckupReminderWorker>()
            .setInitialDelay(90, TimeUnit.DAYS)
            .setInputData(input)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            workName(treeId),
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun cancelForTree(context: Context, treeId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(treeId))
    }
}
