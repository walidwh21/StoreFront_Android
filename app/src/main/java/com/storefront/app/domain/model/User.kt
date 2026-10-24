package com.storefront.app.domain.model

/** An account profile. Stored in the `users` collection, keyed by Firebase Auth uid. */
data class User(
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String? = null,
    val address: Address? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val role: UserRole = UserRole.CUSTOMER
)

enum class UserRole { ADMIN, CUSTOMER, DELIVERY }
