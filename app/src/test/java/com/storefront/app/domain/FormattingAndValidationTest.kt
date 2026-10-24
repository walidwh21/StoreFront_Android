package com.storefront.app.domain

import com.storefront.app.core.format.asPrice
import com.storefront.app.core.format.asRating
import com.storefront.app.core.util.isValidEmail
import com.storefront.app.domain.model.Address
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FormattingAndValidationTest {

    @Test
    fun `prices always show two decimals with a dollar sign`() {
        assertEquals("\$12.50", 12.5.asPrice())
        assertEquals("\$0.00", 0.0.asPrice())
        assertEquals("\$1999.99", 1999.99.asPrice())
    }

    @Test
    fun `ratings show one decimal or a dash`() {
        assertEquals("4.3", 4.26.asRating())
        assertEquals("–", (null as Double?).asRating())
    }

    @Test
    fun `email validation`() {
        assertTrue("shopper@example.com".isValidEmail())
        assertTrue("  shopper@example.com ".isValidEmail())
        assertFalse("shopper@".isValidEmail())
        assertFalse("shopper example.com".isValidEmail())
        assertFalse("".isValidEmail())
    }

    @Test
    fun `address one-liner skips blanks`() {
        assertEquals("1 Main St, Springfield, 12345, USA", Address("1 Main St", "Springfield", "12345", "USA").oneLine())
        assertEquals("Springfield, USA", Address("", "Springfield", null, "USA").oneLine())
    }
}
