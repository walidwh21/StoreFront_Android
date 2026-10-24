package com.storefront.app.domain

import com.storefront.app.domain.model.Product
import com.storefront.app.domain.model.finalPrice
import com.storefront.app.domain.model.gallery
import com.storefront.app.domain.model.hasDiscount
import com.storefront.app.domain.model.isRunningLow
import com.storefront.app.domain.model.isSoldOut
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductPricingTest {

    @Test
    fun `discount is taken off the list price`() {
        val product = Product(price = 100.0, discountPercentage = 20.0)
        assertEquals(80.0, product.finalPrice, 0.0001)
        assertTrue(product.hasDiscount)
    }

    @Test
    fun `missing or negative discount leaves the price alone`() {
        assertEquals(50.0, Product(price = 50.0, discountPercentage = null).finalPrice, 0.0001)
        assertEquals(50.0, Product(price = 50.0, discountPercentage = -15.0).finalPrice, 0.0001)
        assertFalse(Product(price = 50.0, discountPercentage = -15.0).hasDiscount)
    }

    @Test
    fun `stock thresholds`() {
        assertTrue(Product(stock = 0).isSoldOut)
        assertFalse(Product(stock = 0).isRunningLow)
        assertTrue(Product(stock = 9).isRunningLow)
        assertFalse(Product(stock = 10).isRunningLow)
    }

    @Test
    fun `gallery falls back to the thumbnail`() {
        assertEquals(listOf("thumb"), Product(thumbnail = "thumb").gallery)
        assertEquals(listOf("a", "b"), Product(thumbnail = "thumb", images = listOf("a", "b")).gallery)
    }
}
