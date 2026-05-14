package com.nammahasiru.app.reminders

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nammahasiru.app.R
import com.nammahasiru.app.data.TreeDatabase
import com.nammahasiru.app.data.TreeStatus

class CheckupReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val treeId = inputData.getLong(KEY_TREE_ID, -1L)
        if (treeId < 0) return Result.success()

        val dao = TreeDatabase.build(applicationContext).treeDao()
        val tree = dao.getById(treeId) ?: return Result.success()
        val status = TreeStatus.fromStorage(tree.status)
        if (status != TreeStatus.PLANTED) return Result.success()

        val title = applicationContext.getString(R.string.checkup_notification_title)
        val body = applicationContext.getString(R.string.checkup_notification_body, tree.speciesName)

        val notification = NotificationCompat.Builder(applicationContext, CheckupReminderScheduler.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.notify(treeId.toInt(), notification)

        return Result.success()
    }

    companion object {
        const val KEY_TREE_ID = "tree_id"
    }
}
