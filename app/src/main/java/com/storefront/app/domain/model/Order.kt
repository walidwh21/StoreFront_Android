package com.storefront.app.domain.model

import java.util.Date

/** A placed order. Stored in the `orders` Firestore collection. */
data class Order(
    val orderId: String = "",
    val orderDate: Date = Date(),
    val items: List<OrderLine> = emptyList(),
    val totalAmount: Double = 0.0,
    val shippingFee: Double = 0.0,
    val deliveryAddress: Address = Address(),
    val paymentMethod: PaymentMethod = PaymentMethod.CASH_ON_DELIVERY,
    val userId: String = "",
    val status: OrderStatus = OrderStatus.PENDING
)

/** Price and discount are frozen at purchase time so later edits don't rewrite history. */
data class OrderLine(
    val product: Product = Product(),
    val quantity: Int = 0,
    val unitPrice: Double = 0.0,
    val discountApplied: Double = 0.0
)

enum class OrderStatus(val label: String) {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled")
}

enum class PaymentMethod(val label: String) {
    CASH_ON_DELIVERY("Cash on delivery"),
    CREDIT_CARD("Credit card"),
    PAYPAL("PayPal"),
    STRIPE("Stripe")
}
