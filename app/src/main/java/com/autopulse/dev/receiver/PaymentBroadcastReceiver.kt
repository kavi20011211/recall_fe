package com.autopulse.dev.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import co.poynt.os.model.Intents
import co.poynt.os.model.Payment
import com.autopulse.dev.model.TransactionData
import com.autopulse.dev.notification.NotificationHelper

class PaymentBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intents.ACTION_TRANSACTION_SUCCESS) {
            handlePoyntPayment(context, intent)
        }
    }

    private fun handlePoyntPayment(context: Context, intent: Intent) {
        val payment: Payment? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intents.INTENT_EXTRAS_PAYMENT, Payment::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intents.INTENT_EXTRAS_PAYMENT)
        }

        val txn = payment?.transactions?.firstOrNull() ?: return
        val transactionId = txn.id?.toString()?.takeIf { it.isNotEmpty() } ?: return
        val amountCents = txn.amounts?.transactionAmount ?: return
        val card = txn.fundingSource?.card
        // Use card ID as the customer card identifier (Poynt's internal unique card reference)
        val cardToken = card?.id?.toString() ?: ""

        NotificationHelper.showPaymentNotification(
            context,
            TransactionData(
                transactionId = transactionId,
                amount = "%.2f".format(amountCents / 100.0),
                cardToken = cardToken
            )
        )
    }
}