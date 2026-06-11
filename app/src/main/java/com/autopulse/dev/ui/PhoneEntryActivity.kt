package com.autopulse.dev.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autopulse.dev.notification.NotificationHelper
import com.autopulse.dev.ui.theme.RecallApplicationGodaddyPoyntTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PhoneEntryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val transactionId = intent.getStringExtra(NotificationHelper.EXTRA_TRANSACTION_ID) ?: ""
        val amount = intent.getStringExtra(NotificationHelper.EXTRA_AMOUNT) ?: ""
        val cardToken = intent.getStringExtra(NotificationHelper.EXTRA_CARD_TOKEN) ?: ""

        setContent {
            RecallApplicationGodaddyPoyntTheme {
                PhoneEntryScreen(
                    onConfirm = { phoneNumber ->
                        val consentTimestamp = currentIsoTimestamp()
                        startActivity(Intent(this, ServiceSelectionActivity::class.java).apply {
                            putExtra(NotificationHelper.EXTRA_TRANSACTION_ID, transactionId)
                            putExtra(NotificationHelper.EXTRA_AMOUNT, amount)
                            putExtra(NotificationHelper.EXTRA_CARD_TOKEN, cardToken)
                            putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
                            putExtra(EXTRA_CONSENT_TIMESTAMP, consentTimestamp)
                        })
                        finish()
                    },
                    onBack = { finish() }
                )
            }
        }
    }

    companion object {
        const val EXTRA_PHONE_NUMBER = "phone_number"
        const val EXTRA_CONSENT_TIMESTAMP = "consent_timestamp"
    }
}

private fun currentIsoTimestamp(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date())
}

@Composable
fun PhoneEntryScreen(onConfirm: (String) -> Unit, onBack: () -> Unit) {
    var digits by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Enter Customer Phone",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = formatPhoneNumber(digits).ifEmpty { "(___) ___-____" },
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = if (digits.isEmpty()) MaterialTheme.colorScheme.outline
                    else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Text(
                    text = "By entering your number, this customer agrees to receive automated service " +
                            "reminders from this business. Reply STOP to opt out at any time.",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            NumericKeypad(
                onDigit = { if (digits.length < 10) digits += it },
                onBackspace = { if (digits.isNotEmpty()) digits = digits.dropLast(1) }
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(60.dp)
                ) {
                    Text("Back", fontSize = 18.sp)
                }
                Button(
                    onClick = { if (digits.length == 10) onConfirm(digits) },
                    modifier = Modifier.weight(2f).height(60.dp),
                    enabled = digits.length == 10
                ) {
                    Text("Confirm", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun NumericKeypad(onDigit: (String) -> Unit, onBackspace: () -> Unit) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "⌫")
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { key ->
                    when {
                        key.isEmpty() -> Spacer(modifier = Modifier.weight(1f))
                        key == "⌫" -> Button(
                            onClick = onBackspace,
                            modifier = Modifier.weight(1f).height(68.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) { Text(key, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
                        else -> Button(
                            onClick = { onDigit(key) },
                            modifier = Modifier.weight(1f).height(68.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) { Text(key, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

fun formatPhoneNumber(digits: String): String = when {
    digits.length <= 3 -> digits
    digits.length <= 6 -> "(${digits.take(3)}) ${digits.drop(3)}"
    else -> "(${digits.take(3)}) ${digits.substring(3, 6)}-${digits.drop(6)}"
}
