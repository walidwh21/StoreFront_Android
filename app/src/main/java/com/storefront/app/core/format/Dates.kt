package com.storefront.app.core.format

import java.text.DateFormat
import java.util.Date

/** Medium date + short time in the device locale, e.g. "Mar 4, 2026, 3:20 PM". */
fun Date.asDateTime(): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(this)

fun Long.asDateTime(): String = Date(this).asDateTime()
