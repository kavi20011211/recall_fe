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
import kotlinx.coroutines.delay

class CapturePromptActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val transactionId = intent.getStringExtra(NotificationHelper.EXTRA_TRANSACTION_ID) ?: ""
        val amount = intent.getStringExtra(NotificationHelper.EXTRA_AMOUNT) ?: "0.00"
        val cardToken = intent.getStringExtra(NotificationHelper.EXTRA_CARD_TOKEN) ?: ""

        setContent {
            RecallApplicationGodaddyPoyntTheme {
                CapturePromptScreen(
                    amount = amount,
                    onYes = {
                        startActivity(Intent(this, PhoneEntryActivity::class.java).apply {
                            putExtra(NotificationHelper.EXTRA_TRANSACTION_ID, transactionId)
                            putExtra(NotificationHelper.EXTRA_AMOUNT, amount)
                            putExtra(NotificationHelper.EXTRA_CARD_TOKEN, cardToken)
                        })
                        finish()
                    },
                    onSkip = { finish() }
                )
            }
        }
    }
}

@Composable
fun CapturePromptScreen(
    amount: String,
    onYes: () -> Unit,
    onSkip: () -> Unit
) {
    var secondsRemaining by remember { mutableIntStateOf(10) }
    var dismissed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (secondsRemaining > 0 && !dismissed) {
            delay(1000)
            if (!dismissed) secondsRemaining--
        }
        if (!dismissed) {
            dismissed = true
            onSkip()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Payment Approved",
                        fontSize = 20.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\$$amount",
                        fontSize = 42.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Would you like to save\nthis customer's info?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Auto-dismisses in $secondsRemaining second${if (secondsRemaining == 1) "" else "s"}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    dismissed = true
                    onYes()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) {
                Text(
                    text = "Yes — Save Customer",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    dismissed = true
                    onSkip()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            ) {
                Text(text = "Skip", fontSize = 20.sp)
            }
        }
    }
}