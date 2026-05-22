package com.example.recallapplicationgodaddypoynt.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recallapplicationgodaddypoynt.notification.NotificationHelper
import com.example.recallapplicationgodaddypoynt.ui.theme.RecallApplicationGodaddyPoyntTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ServiceSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val transactionId = intent.getStringExtra(NotificationHelper.EXTRA_TRANSACTION_ID) ?: ""
        val cardToken = intent.getStringExtra(NotificationHelper.EXTRA_CARD_TOKEN) ?: ""
        val phoneNumber = intent.getStringExtra(PhoneEntryActivity.EXTRA_PHONE_NUMBER) ?: ""
        val consentTimestamp = intent.getStringExtra(PhoneEntryActivity.EXTRA_CONSENT_TIMESTAMP) ?: ""

        setContent {
            RecallApplicationGodaddyPoyntTheme {
                ServiceSelectionScreen(
                    onConfirm = { serviceDisplayName, reminderDays ->
                        val reminderDate = LocalDate.now().plusDays(reminderDays.toLong())
                        // ISO format for API: "YYYY-MM-DD"
                        val reminderDateIso = reminderDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        // API enum value: "oil" | "brakes" | "tires" | "general"
                        val serviceTypeApi = mapServiceTypeToApi(serviceDisplayName)

                        startActivity(Intent(this, SuccessActivity::class.java).apply {
                            putExtra(NotificationHelper.EXTRA_TRANSACTION_ID, transactionId)
                            putExtra(NotificationHelper.EXTRA_CARD_TOKEN, cardToken)
                            putExtra(PhoneEntryActivity.EXTRA_PHONE_NUMBER, phoneNumber)
                            putExtra(PhoneEntryActivity.EXTRA_CONSENT_TIMESTAMP, consentTimestamp)
                            putExtra(SuccessActivity.EXTRA_SERVICE_TYPE, serviceDisplayName)
                            putExtra(SuccessActivity.EXTRA_SERVICE_TYPE_API, serviceTypeApi)
                            putExtra(SuccessActivity.EXTRA_REMINDER_DATE_ISO, reminderDateIso)
                        })
                        finish()
                    },
                    onBack = { finish() }
                )
            }
        }
    }

    private fun mapServiceTypeToApi(displayName: String): String = when (displayName) {
        "Oil Change" -> "oil"
        "Brakes" -> "brakes"
        "Tires" -> "tires"
        else -> "general"   // covers "General / Other" and "Test (Dev)"
    }
}

private data class ServiceOption(
    val name: String,
    val reminderDays: Int,
    val color: Color
)

@Composable
fun ServiceSelectionScreen(
    onConfirm: (serviceDisplayName: String, reminderDays: Int) -> Unit,
    onBack: () -> Unit
) {
    val mainServices = listOf(
        ServiceOption("Oil Change", 90, Color(0xFF1565C0)),
        ServiceOption("Brakes", 365, Color(0xFFB71C1C)),
        ServiceOption("Tires", 180, Color(0xFF2E7D32)),
        ServiceOption("General / Other", 60, Color(0xFF4A148C))
    )
    val testService = ServiceOption("Test (Dev)", 0, Color(0xFFE65100))

    var selected by remember { mutableStateOf<ServiceOption?>(null) }

    val reminderLabel = selected?.let { svc ->
        if (svc.reminderDays == 0) "Today — reminder queued for same day (dev test)"
        else LocalDate.now().plusDays(svc.reminderDays.toLong())
            .format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text("Select Service Type", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "What service was performed today?",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 2×2 production services
            mainServices.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { svc ->
                        val isSelected = selected == svc
                        Button(
                            onClick = { selected = svc },
                            modifier = Modifier.weight(1f).height(108.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) svc.color else svc.color.copy(alpha = 0.12f),
                                contentColor = if (isSelected) Color.White else svc.color
                            )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(svc.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 20.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("${svc.reminderDays} days", fontSize = 13.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Dev test service — full width, distinct amber style
            val isTestSelected = selected == testService
            Button(
                onClick = { selected = testService },
                modifier = Modifier.fillMaxWidth().height(72.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTestSelected) testService.color else testService.color.copy(alpha = 0.12f),
                    contentColor = if (isTestSelected) Color.White else testService.color
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("DEV", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text("Test Service", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("Reminder fires today (1 min test)", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (reminderLabel != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected == testService)
                            Color(0xFFE65100).copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "Reminder: $reminderLabel",
                        modifier = Modifier.padding(14.dp),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (selected == testService) Color(0xFFE65100)
                        else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

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
                    onClick = {
                        val svc = selected ?: return@Button
                        onConfirm(svc.name, svc.reminderDays)
                    },
                    modifier = Modifier.weight(2f).height(60.dp),
                    enabled = selected != null
                ) {
                    Text("Confirm", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
