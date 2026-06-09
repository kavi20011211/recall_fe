package com.example.recallapplicationgodaddypoynt.model

data class TransactionData(
    val transactionId: String,
    val amount: String,
    val cardToken: String,
    val isSimulated: Boolean = false
)
