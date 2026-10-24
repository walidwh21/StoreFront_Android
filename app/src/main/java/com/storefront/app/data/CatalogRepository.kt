package com.storefront.app.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.storefront.app.core.firebase.awaitResult
import com.storefront.app.domain.model.Product
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepository @Inject constructor(
    db: FirebaseFirestore
) {
    private val products = db.collection("products")

    suspend fun fetchAll(): List<Product> =
        products.get().awaitResult().documents.mapNotNull { it.toProduct() }

    suspend fun fetchById(id: String): Product? =
        products.document(id).get().awaitResult().toProduct()

    /** Adds [amount] units using a server-side increment, so concurrent edits don't clobber each other. */
    suspend fun restock(id: String, amount: Int, at: Date) {
        products.document(id)
            .update(mapOf("stock" to FieldValue.increment(amount.toLong()), "restockDate" to at))
            .awaitResult()
    }

    suspend fun markSoldOut(id: String, at: Date) {
        products.document(id)
            .update(mapOf("stock" to 0, "restockDate" to at))
            .awaitResult()
    }

    private fun DocumentSnapshot.toProduct(): Product? =
        toObject(Product::class.java)?.let { if (it.id.isBlank()) it.copy(id = id) else it }
}
