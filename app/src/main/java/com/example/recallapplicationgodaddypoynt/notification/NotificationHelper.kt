package com.example.recallapplicationgodaddypoynt.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.recallapplicationgodaddypoynt.model.TransactionData
import com.example.recallapplicationgodaddypoynt.ui.CapturePromptActivity

object NotificationHelper {

    private const val CHANNEL_ID = "recall_payment_events"
    private const val CHANNEL_NAME = "Payment Events"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for completed payment transactions"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun showPaymentNotification(context: Context, transaction: TransactionData) {
        val intent = Intent(context, CapturePromptActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TRANSACTION_ID, transaction.transactionId)
            putExtra(EXTRA_AMOUNT, transaction.amount)
            putExtra(EXTRA_CARD_TOKEN, transaction.cardToken)
            putExtra(EXTRA_IS_SIMULATED, transaction.isSimulated)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val label = if (transaction.isSimulated) "[SIM] " else ""
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("${label}Payment Approved — \$${transaction.amount}")
            .setContentText("Tap to save customer info")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, notification)
    }

    const val EXTRA_TRANSACTION_ID = "transaction_id"
    const val EXTRA_AMOUNT = "amount"
    const val EXTRA_CARD_TOKEN = "card_token"
    const val EXTRA_IS_SIMULATED = "is_simulated"
}
