package com.example.recallapplicationgodaddypoynt

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recallapplicationgodaddypoynt.model.TransactionData
import com.example.recallapplicationgodaddypoynt.notification.NotificationHelper
import com.example.recallapplicationgodaddypoynt.storage.AppPreferences
import com.example.recallapplicationgodaddypoynt.ui.OnboardingActivity
import com.example.recallapplicationgodaddypoynt.ui.theme.RecallApplicationGodaddyPoyntTheme
import java.util.UUID

class MainActivity : ComponentActivity() {

    private val prefs by lazy { AppPreferences(this) }

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* handled silently */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!prefs.isOnboarded) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        NotificationHelper.createNotificationChannel(this)
        requestNotificationPermissionIfNeeded()

        setContent {
            RecallApplicationGodaddyPoyntTheme {
                MainScreen(
                    businessName = prefs.businessName ?: "Your Business",
                    onSimulatePayment = ::simulatePayment
                )
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun simulatePayment() {
        val transaction = TransactionData(
            transactionId = "SIM-${UUID.randomUUID().toString().take(8).uppercase()}",
            amount = String.format("%.2f", (1500..25000).random() / 100.0),
            cardToken = "TOKEN-SIM-${System.currentTimeMillis()}",
            isSimulated = true
        )
        NotificationHelper.showPaymentNotification(this, transaction)
    }
}

@Composable
fun MainScreen(businessName: String, onSimulatePayment: () -> Unit) {
    var notificationSent by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Recall",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "GoDaddy Poynt",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = businessName,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(48.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "DEV MODE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap below to simulate a completed payment and test the full capture flow.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            notificationSent = true
                            onSimulatePayment()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                    ) {
                        Text(
                            text = "Simulate Payment",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (notificationSent) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Notification sent — check your notification tray",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "In production, Recall listens for GoDaddy Poynt payment broadcasts automatically.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )
        }
    }
}
