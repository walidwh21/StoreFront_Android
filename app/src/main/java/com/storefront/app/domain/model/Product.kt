package com.storefront.app.domain.model

import java.util.Date

/** A catalogue entry. Stored in the `products` Firestore collection. */
data class Product(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    val price: Double = 0.0,
    val discountPercentage: Double? = null,
    val rating: Double? = null,
    val stock: Int = 0,
    val restockDate: Date? = null,
    val brand: String = "",
    val category: String = "",
    val thumbnail: String = "",
    val images: List<String> = emptyList()
)

// Derived values live in extension properties so Firestore never tries to
// serialise them as document fields.

/** Percentage off, never negative. */
val Product.discount: Double get() = (discountPercentage ?: 0.0).coerceAtLeast(0.0)

val Product.hasDiscount: Boolean get() = discount > 0.0

/** Price after the discount has been applied. */
val Product.finalPrice: Double get() = price * (1.0 - discount / 100.0)

val Product.isSoldOut: Boolean get() = stock <= 0

val Product.isRunningLow: Boolean get() = stock in 1 until LOW_STOCK_THRESHOLD

/** Images for the gallery, falling back to the thumbnail. */
val Product.gallery: List<String> get() = images.ifEmpty { listOf(thumbnail) }

const val LOW_STOCK_THRESHOLD = 10
