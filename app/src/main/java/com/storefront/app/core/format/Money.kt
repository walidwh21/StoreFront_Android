package com.storefront.app.core.format

import java.util.Locale

/** Formats an amount as a dollar price with two decimals, e.g. `$12.50`. */
fun Double.asPrice(): String = String.format(Locale.US, "\$%.2f", this)

/** Formats a rating like `4.3`, or an en dash when the product has none yet. */
fun Double?.asRating(): String =
    if (this == null) "–" else String.format(Locale.US, "%.1f", this)
