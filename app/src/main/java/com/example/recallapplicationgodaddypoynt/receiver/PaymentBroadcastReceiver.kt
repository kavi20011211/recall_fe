package com.example.recallapplicationgodaddypoynt.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.recallapplicationgodaddypoynt.model.TransactionData
import com.example.recallapplicationgodaddypoynt.notification.NotificationHelper

class PaymentBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_SIMULATE_PAYMENT -> handleSimulatedPayment(context, intent)
            ACTION_POYNT_TRANSACTION_COMPLETED -> handlePoyntPayment(context, intent)
        }
    }

    private fun handleSimulatedPayment(context: Context, intent: Intent) {
        val transaction = TransactionData(
            transactionId = intent.getStringExtra("transaction_id") ?: "SIM-${System.currentTimeMillis()}",
            amount = intent.getStringExtra("amount") ?: "0.00",
            cardToken = intent.getStringExtra("card_token") ?: "TOKEN-SIM",
            isSimulated = true
        )
        NotificationHelper.showPaymentNotification(context, transaction)
    }

    private fun handlePoyntPayment(context: Context, intent: Intent) {
        // Real Poynt SDK: extract transaction from the Poynt intent bundle.
        // The Poynt SDK broadcasts TRANSACTION_COMPLETED with a Transaction parcelable.
        // TODO: replace with real Poynt Transaction parcelable extraction when running on device
        val transaction = TransactionData(
            transactionId = intent.getStringExtra("transactionId") ?: "",
            amount = intent.getStringExtra("amount") ?: "0.00",
            cardToken = intent.getStringExtra("cardToken") ?: "",
            isSimulated = false
        )
        if (transaction.transactionId.isNotEmpty()) {
            NotificationHelper.showPaymentNotification(context, transaction)
        }
    }

    companion object {
        const val ACTION_SIMULATE_PAYMENT = "com.recall.poynt.SIMULATE_PAYMENT"
        // Real GoDaddy Poynt broadcast for completed transactions
        const val ACTION_POYNT_TRANSACTION_COMPLETED = "com.poynt.terminal.action.TRANSACTION_COMPLETED"
    }
}
