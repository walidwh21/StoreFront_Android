package com.storefront.app.domain

import com.storefront.app.domain.model.Address
import com.storefront.app.domain.model.CartLine
import com.storefront.app.domain.model.OrderFactory
import com.storefront.app.domain.model.OrderStatus
import com.storefront.app.domain.model.PaymentMethod
import com.storefront.app.domain.model.Product
import org.junit.Assert.assertEquals
import org.junit.Test

class OrderFactoryTest {

    private val lines = listOf(
        CartLine(Product(id = "a", price = 100.0, discountPercentage = 25.0, stock = 9), quantity = 2),
        CartLine(Product(id = "b", price = 10.0, stock = 9), quantity = 3)
    )

    @Test
    fun `prices and discounts are frozen on the order lines`() {
        val order = OrderFactory.fromCart("u1", lines, Address(), PaymentMethod.PAYPAL)

        assertEquals(75.0, order.items[0].unitPrice, 0.0001)
        assertEquals(25.0, order.items[0].discountApplied, 0.0001)
        assertEquals(10.0, order.items[1].unitPrice, 0.0001)
        assertEquals(0.0, order.items[1].discountApplied, 0.0001)
    }

    @Test
    fun `total is the sum of lines plus shipping`() {
        val order = OrderFactory.fromCart("u1", lines, Address(), PaymentMethod.STRIPE, shippingFee = 5.0)

        // 2 x 75 + 3 x 10 + 5
        assertEquals(185.0, order.totalAmount, 0.0001)
        assertEquals(5.0, order.shippingFee, 0.0001)
    }

    @Test
    fun `new orders start pending and carry the owner and id`() {
        val order = OrderFactory.fromCart("u42", lines, Address(), PaymentMethod.CASH_ON_DELIVERY, orderId = "o-1")

        assertEquals(OrderStatus.PENDING, order.status)
        assertEquals("u42", order.userId)
        assertEquals("o-1", order.orderId)
    }
}
