package com.storefront.app.core.util

private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

/** Pure-Kotlin check (no android.util.Patterns) so it can be unit-tested on the JVM. */
fun String.isValidEmail(): Boolean = EMAIL.matches(trim())

/** Firebase Auth rejects passwords shorter than six characters. */
const val MIN_PASSWORD_LENGTH = 6
