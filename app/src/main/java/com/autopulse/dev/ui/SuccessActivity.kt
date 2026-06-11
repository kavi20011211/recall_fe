package com.autopulse.dev.ui

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
import androidx.lifecycle.lifecycleScope
import com.autopulse.dev.MainActivity
import com.autopulse.dev.api.ApiClient
import com.autopulse.dev.api.CreateCustomerRequest
import com.autopulse.dev.api.CreateVisitRequest
import com.autopulse.dev.storage.AppPreferences
import com.autopulse.dev.ui.theme.RecallApplicationGodaddyPoyntTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class SaveState {
    object Loading : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}

class SuccessActivity : ComponentActivity() {

    private val prefs by lazy { AppPreferences(this) }
    private var saveState by mutableStateOf<SaveState>(SaveState.Loading)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val phoneNumber = intent.getStringExtra(PhoneEntryActivity.EXTRA_PHONE_NUMBER) ?: ""
        val consentTimestamp = intent.getStringExtra(PhoneEntryActivity.EXTRA_CONSENT_TIMESTAMP) ?: ""
        val serviceType = intent.getStringExtra(EXTRA_SERVICE_TYPE) ?: ""
        val serviceTypeApi = intent.getStringExtra(EXTRA_SERVICE_TYPE_API) ?: "general"
        val reminderDateIso = intent.getStringExtra(EXTRA_REMINDER_DATE_ISO) ?: ""

        setContent {
            RecallApplicationGodaddyPoyntTheme {
                SuccessScreen(
                    saveState = saveState,
                    serviceType = serviceType,
                    reminderDateIso = reminderDateIso,
                    phoneNumber = phoneNumber,
                    onRetry = {
                        saveState = SaveState.Loading
                        lifecycleScope.launch {
                            saveState = saveCustomerAndVisit(phoneNumber, consentTimestamp, serviceTypeApi, reminderDateIso)
                        }
                    },
                    onDismiss = { navigateHome() }
                )
            }
        }

        lifecycleScope.launch {
            saveState = saveCustomerAndVisit(phoneNumber, consentTimestamp, serviceTypeApi, reminderDateIso)
        }
    }

    private suspend fun saveCustomerAndVisit(
        phoneNumber: String,
        consentTimestamp: String,
        serviceTypeApi: String,
        reminderDateIso: String
    ): SaveState {
        if (!prefs.isOnboarded) {
            return SaveState.Error("Not registered. Please restart the app.")
        }

        return try {
            // Step 1: Create or find the customer
            val customerResponse = ApiClient.service.createCustomer(
                token = prefs.bearerToken,
                request = CreateCustomerRequest(
                    merchant_id = prefs.merchantId,
                    phone_number = "+1$phoneNumber",  // Canadian E.164
                    first_name = null,
                    consent_timestamp = consentTimestamp
                )
            )

            if (!customerResponse.isSuccessful && customerResponse.code() != 409) {
                val msg = customerResponse.body()?.message
                    ?: customerResponse.errorBody()?.string()
                    ?: "Failed to save customer (${customerResponse.code()})"
                return SaveState.Error(msg)
            }

            val customerId = customerResponse.body()?.resolvedCustomerId
                ?: return SaveState.Error("Could not determine customer ID.")

            // Step 2: Log the visit
            val visitResponse = ApiClient.service.createVisit(
                token = prefs.bearerToken,
                request = CreateVisitRequest(
                    customer_id = customerId,
                    service_type = serviceTypeApi,
                    reminder_date = reminderDateIso
                )
            )

            if (visitResponse.isSuccessful && visitResponse.body()?.success == true) {
                SaveState.Success
            } else {
                val msg = visitResponse.body()?.message
                    ?: visitResponse.errorBody()?.string()
                    ?: "Failed to log visit (${visitResponse.code()})"
                SaveState.Error(msg)
            }
        } catch (e: Exception) {
            SaveState.Error("Network error: ${e.message ?: "Cannot reach server"}")
        }
    }

    private fun navigateHome() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        })
        finish()
    }

    companion object {
        const val EXTRA_SERVICE_TYPE = "service_type"
        const val EXTRA_SERVICE_TYPE_API = "service_type_api"
        const val EXTRA_REMINDER_DATE_ISO = "reminder_date_iso"
    }
}

@Composable
fun SuccessScreen(
    saveState: SaveState,
    serviceType: String,
    reminderDateIso: String,
    phoneNumber: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    when (saveState) {
        is SaveState.Loading -> SavingLoadingContent()
        is SaveState.Success -> SuccessContent(serviceType, reminderDateIso, phoneNumber, onDismiss)
        is SaveState.Error -> ErrorContent(saveState.message, onRetry, onDismiss)
    }
}

@Composable
private fun SavingLoadingContent() {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(56.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text("Saving customer...", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SuccessContent(
    serviceType: String,
    reminderDateIso: String,
    phoneNumber: String,
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(3000)
        onDismiss()
    }

    val displayDate = try {
        LocalDate.parse(reminderDateIso).format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
    } catch (e: Exception) {
        reminderDateIso
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF1B5E20)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("✓", fontSize = 88.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Customer Saved!", fontSize = 34.sp, color = Color.White, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Service", fontSize = 13.sp, color = Color(0xFFA5D6A7))
                    Text(serviceType, fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFF4CAF50))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Reminder Date", fontSize = 13.sp, color = Color(0xFFA5D6A7))
                    Text(displayDate, fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)

                    if (phoneNumber.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Phone", fontSize = 13.sp, color = Color(0xFFA5D6A7))
                        Text(formatPhoneNumber(phoneNumber), fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text("Returning to home screen...", color = Color(0xFF81C784), fontSize = 14.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit, onSkip: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("⚠", fontSize = 64.sp, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Could Not Save Customer", fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Retry", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Skip — Return to Home", fontSize = 16.sp)
            }
        }
    }
}
