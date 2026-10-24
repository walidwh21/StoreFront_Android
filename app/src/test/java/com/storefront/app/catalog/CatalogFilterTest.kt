package com.storefront.app.catalog

import com.storefront.app.domain.model.Product
import com.storefront.app.feature.catalog.CatalogFilter
import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogFilterTest {

    private val items = listOf(
        Product(id = "1", title = "Red Lipstick", brand = "Acme", category = "beauty"),
        Product(id = "2", title = "Rose Perfume", brand = "Bloom", category = "fragrances"),
        Product(id = "3", title = "Night Cream", brand = "Acme", category = "skin-care")
    )

    private fun ids(filter: CatalogFilter) = filter.filter(items).map { it.id }

    @Test
    fun `empty filter keeps everything`() {
        assertEquals(listOf("1", "2", "3"), ids(CatalogFilter()))
    }

    @Test
    fun `search matches title brand and category ignoring case`() {
        assertEquals(listOf("1"), ids(CatalogFilter(query = "LIPSTICK")))
        assertEquals(listOf("2"), ids(CatalogFilter(query = "bloom")))
        assertEquals(listOf("3"), ids(CatalogFilter(query = "skin")))
    }

    @Test
    fun `category and brand narrow the list together`() {
        assertEquals(listOf("1", "3"), ids(CatalogFilter(brand = "Acme")))
        assertEquals(listOf("3"), ids(CatalogFilter(brand = "Acme", category = "skin-care")))
        assertEquals(emptyList<String>(), ids(CatalogFilter(brand = "Bloom", category = "beauty")))
    }
}
