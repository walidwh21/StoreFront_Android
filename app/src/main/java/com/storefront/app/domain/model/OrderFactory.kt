package com.storefront.app.domain.model

import java.util.Date
import java.util.UUID

/** Turns what is in the cart into an [Order], freezing prices and discounts as they are right now. */
object OrderFactory {

    fun fromCart(
        userId: String,
        lines: List<CartLine>,
        address: Address,
        paymentMethod: PaymentMethod,
        shippingFee: Double = 0.0,
        now: Date = Date(),
        orderId: String = UUID.randomUUID().toString()
    ): Order {
        val items = lines.map { line ->
            OrderLine(
                product = line.product,
                quantity = line.quantity,
                unitPrice = line.product.finalPrice,
                discountApplied = line.product.discount
            )
        }
        return Order(
            orderId = orderId,
            orderDate = now,
            items = items,
            totalAmount = items.sumOf { it.unitPrice * it.quantity } + shippingFee,
            shippingFee = shippingFee,
            deliveryAddress = address,
            paymentMethod = paymentMethod,
            userId = userId,
            status = OrderStatus.PENDING
        )
    }
}
