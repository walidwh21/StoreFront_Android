package com.storefront.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.storefront.app.core.firebase.awaitResult
import com.storefront.app.domain.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val users = db.collection("users")

    /** Uid of the session Firebase restored from disk, if any. */
    fun restoredUid(): String? = auth.currentUser?.uid

    suspend fun signUp(profile: User, password: String): User {
        val uid = auth.createUserWithEmailAndPassword(profile.email, password)
            .awaitResult().user?.uid
            ?: error("Account was created but no user id was returned.")
        val saved = profile.copy(userId = uid)
        users.document(uid).set(saved).awaitResult()
        return saved
    }

    suspend fun signIn(email: String, password: String): User {
        val uid = auth.signInWithEmailAndPassword(email, password)
            .awaitResult().user?.uid
            ?: error("Sign-in succeeded but no user id was returned.")
        return fetch(uid) ?: error("No profile exists for this account.")
    }

    fun signOut() = auth.signOut()

    suspend fun fetch(uid: String): User? =
        users.document(uid).get().awaitResult().toObject(User::class.java)

    suspend fun fetchAll(): List<User> =
        users.get().awaitResult().toObjects(User::class.java)

    suspend fun update(user: User) {
        users.document(user.userId).update(
            mapOf(
                "fullName" to user.fullName,
                "email" to user.email,
                "phoneNumber" to user.phoneNumber,
                "address" to user.address,
                "role" to user.role.name
            )
        ).awaitResult()
    }
}
