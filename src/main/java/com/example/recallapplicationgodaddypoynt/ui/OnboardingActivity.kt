package com.example.recallapplicationgodaddypoynt.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.recallapplicationgodaddypoynt.MainActivity
import com.example.recallapplicationgodaddypoynt.api.ApiClient
import com.example.recallapplicationgodaddypoynt.api.CreateMerchantRequest
import com.example.recallapplicationgodaddypoynt.storage.AppPreferences
import com.example.recallapplicationgodaddypoynt.ui.theme.RecallApplicationGodaddyPoyntTheme
import kotlinx.coroutines.launch

class OnboardingActivity : ComponentActivity() {

    private val prefs by lazy { AppPreferences(this) }

    private var isLoading by mutableStateOf(false)
    private var errorMessage by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (prefs.isOnboarded) {
            goToMain()
            return
        }

        setContent {
            RecallApplicationGodaddyPoyntTheme {
                OnboardingScreen(
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onRegister = { businessName, ownerPhone, businessPhone ->
                        lifecycleScope.launch {
                            isLoading = true
                            errorMessage = null
                            val result = registerMerchant(businessName, ownerPhone, businessPhone)
                            result.fold(
                                onSuccess = { goToMain() },
                                onFailure = { e ->
                                    errorMessage = e.message ?: "Registration failed. Check your connection."
                                    isLoading = false
                                }
                            )
                        }
                    }
                )
            }
        }
    }

    private suspend fun registerMerchant(
        businessName: String,
        ownerPhone: String,
        businessPhone: String
    ): Result<Unit> = try {
        val response = ApiClient.service.createMerchant(
            CreateMerchantRequest(
                owner_phone = "+1$ownerPhone",
                merchant_id = prefs.merchantId,
                business_name = businessName
            )
        )
        if (response.isSuccessful && response.body()?.success == true) {
            val body = response.body()!!
            prefs.jwtToken = body.token
            prefs.businessName = businessName
            prefs.ownerPhone = "+1$ownerPhone"
            prefs.businessPhone = "+1$businessPhone"
            Result.success(Unit)
        } else {
            val msg = response.body()?.message
                ?: response.errorBody()?.string()
                ?: "Registration failed (${response.code()})"
            Result.failure(Exception(msg))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Cannot reach server. Is it running on port 5000?"))
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

@Composable
fun OnboardingScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onRegister: (businessName: String, ownerPhone: String, businessPhone: String) -> Unit
) {
    var businessName by remember { mutableStateOf("") }
    var ownerPhone by remember { mutableStateOf("") }
    var businessPhone by remember { mutableStateOf("") }

    val canSubmit = businessName.isNotBlank()
            && ownerPhone.length == 10
            && businessPhone.length == 10
            && !isLoading

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Recall",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "GoDaddy Poynt",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Set Up Your Business",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "This only needs to be done once.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text("Business Name") },
                placeholder = { Text("e.g. Mike's Auto Repair") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = ownerPhone,
                onValueChange = { if (it.length <= 10 && it.all(Char::isDigit)) ownerPhone = it },
                label = { Text("Owner Phone") },
                placeholder = { Text("10-digit number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                supportingText = {
                    Text(
                        if (ownerPhone.length == 10) formatPhoneNumber(ownerPhone)
                        else "Receives weekly business digest SMS"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = businessPhone,
                onValueChange = { if (it.length <= 10 && it.all(Char::isDigit)) businessPhone = it },
                label = { Text("Business Phone") },
                placeholder = { Text("10-digit number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                supportingText = {
                    Text(
                        if (businessPhone.length == 10) formatPhoneNumber(businessPhone)
                        else "Shown in reminder SMS sent to your customers"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "Your GoDaddy Poynt terminal ID is auto-generated and stays linked to this device.",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onRegister(businessName, ownerPhone, businessPhone) },
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Get Started", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
