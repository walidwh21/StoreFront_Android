package com.storefront.app.feature.account

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

/**
 * Turns Firebase Auth failures into messages a shopper can act on.
 * Matching on exception types (not message text) keeps this stable across SDK updates.
 */
fun Throwable.toAuthMessage(): String = when (this) {
    // Must come before the invalid-credentials case: it is a subclass of it.
    is FirebaseAuthWeakPasswordException -> "That password is too weak. Use at least 6 characters."
    is FirebaseAuthInvalidCredentialsException -> "The email or password is incorrect."
    is FirebaseAuthInvalidUserException -> "No account exists for that email."
    is FirebaseAuthUserCollisionException -> "An account with that email already exists. Try signing in."
    is FirebaseNetworkException -> "No connection. Check your internet and try again."
    else -> message?.takeIf { it.isNotBlank() } ?: "Something went wrong."
}
