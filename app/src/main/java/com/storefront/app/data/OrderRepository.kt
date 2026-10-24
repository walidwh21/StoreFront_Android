package com.storefront.app.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.storefront.app.core.firebase.awaitResult
import com.storefront.app.domain.model.Order
import javax.inject.Inject
import javax.inject.Singleton

/** Thrown when a product no longer has enough units to fulfil an order line. */
class InsufficientStockException(val productTitle: String) :
    Exception("Not enough stock left for \"$productTitle\".")

@Singleton
class OrderRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val orders = db.collection("orders")
    private val products = db.collection("products")

    suspend fun fetchForUser(userId: String): List<Order> =
        orders.whereEqualTo("userId", userId).get().awaitResult()
            .toObjects(Order::class.java)
            .sortedByDescending { it.orderDate }

    /**
     * Writes the order and decrements stock in a single Firestore transaction:
     * either every line is reserved and the order saved, or nothing changes.
     */
    suspend fun place(order: Order) {
        try {
            db.runTransaction(Transaction.Function<Unit> { tx ->
                val refs = order.items.map { products.document(it.product.id) }

                // Firestore requires all reads to happen before any write.
                val available = refs.map { ref -> tx.get(ref).getLong("stock") ?: 0L }

                order.items.forEachIndexed { index, line ->
                    if (available[index] < line.quantity) {
                        throw InsufficientStockException(line.product.title)
                    }
                }
                order.items.forEachIndexed { index, line ->
                    tx.update(refs[index], "stock", available[index] - line.quantity)
                }
                tx.set(orders.document(order.orderId), order)
                Unit
            }).awaitResult()
        } catch (e: Exception) {
            // The SDK may wrap exceptions thrown inside the transaction body.
            throw generateSequence<Throwable>(e) { it.cause }
                .filterIsInstance<InsufficientStockException>()
                .firstOrNull() ?: e
        }
    }
}
