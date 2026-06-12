package com.example.recallapplicationgodaddypoynt.api

// ─── Merchant ────────────────────────────────────────────────────────────────

data class CreateMerchantRequest(
    val owner_phone: String,
    val merchant_id: String,
    val business_name: String
)

data class CreateMerchantResponse(
    val success: Boolean,
    val message: String,
    val merchantId: Int?,
    val token: String?
)

// ─── Customer ────────────────────────────────────────────────────────────────

data class CreateCustomerRequest(
    val merchant_id: String,
    val phone_number: String,
    val first_name: String?,
    val consent_timestamp: String
)

data class ExistingCustomerData(
    val id: Int,
    val first_name: String?,
    val phone_number: String
)

data class CreateCustomerResponse(
    val success: Boolean,
    val message: String,
    val customerId: Int?,        // present on 201 (new customer)
    val customer: ExistingCustomerData? // present on 200 (returning customer)
) {
    val resolvedCustomerId: Int?
        get() = customerId ?: customer?.id
}

// ─── Visit ───────────────────────────────────────────────────────────────────

data class CreateVisitRequest(
    val customer_id: Int,
    val service_type: String,   // "oil" | "brakes" | "tires" | "general"
    val reminder_date: String   // "YYYY-MM-DD"
)

data class CreateVisitResponse(
    val success: Boolean,
    val message: String,
    val visitId: Int?
)
