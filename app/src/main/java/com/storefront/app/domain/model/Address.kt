package com.storefront.app.domain.model

/** Postal address. Property names double as Firestore field names. */
data class Address(
    val street: String = "",
    val city: String = "",
    val postalCode: String? = null,
    val country: String = ""
) {
    fun oneLine(): String =
        listOfNotNull(street, city, postalCode, country)
            .filter { it.isNotBlank() }
            .joinToString(", ")
}
