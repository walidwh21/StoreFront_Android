package com.storefront.app.cart

import com.storefront.app.domain.model.Product
import com.storefront.app.feature.cart.CartAction
import com.storefront.app.feature.cart.CartViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CartViewModelTest {

    private fun product(
        id: String = "p1",
        price: Double = 10.0,
        stock: Int = 5,
        discount: Double? = null
    ) = Product(id = id, title = "Item $id", price = price, stock = stock, discountPercentage = discount)

    private fun CartViewModel.quantityOf(id: String) =
        state.value.lines.firstOrNull { it.product.id == id }?.quantity

    @Test
    fun `adding the same product twice bumps the quantity`() {
        val cart = CartViewModel()
        cart.dispatch(CartAction.Add(product()))
        cart.dispatch(CartAction.Add(product()))

        assertEquals(1, cart.state.value.lines.size)
        assertEquals(2, cart.quantityOf("p1"))
        assertEquals(2, cart.state.value.itemCount)
    }

    @Test
    fun `quantity can never exceed stock`() {
        val cart = CartViewModel()
        repeat(10) { cart.dispatch(CartAction.Add(product(stock = 3))) }
        assertEquals(3, cart.quantityOf("p1"))

        cart.dispatch(CartAction.SetQuantity("p1", 99))
        assertEquals(3, cart.quantityOf("p1"))
    }

    @Test
    fun `sold-out products are ignored`() {
        val cart = CartViewModel()
        cart.dispatch(CartAction.Add(product(stock = 0)))
        assertTrue(cart.state.value.isEmpty)
    }

    @Test
    fun `setting quantity to zero removes the line`() {
        val cart = CartViewModel()
        cart.dispatch(CartAction.Add(product()))
        cart.dispatch(CartAction.SetQuantity("p1", 0))
        assertTrue(cart.state.value.isEmpty)
    }

    @Test
    fun `remove only affects the chosen product`() {
        val cart = CartViewModel()
        cart.dispatch(CartAction.Add(product("a")))
        cart.dispatch(CartAction.Add(product("b")))
        cart.dispatch(CartAction.Remove("a"))

        assertEquals(null, cart.quantityOf("a"))
        assertEquals(1, cart.quantityOf("b"))
    }

    @Test
    fun `total uses discounted prices`() {
        val cart = CartViewModel()
        cart.dispatch(CartAction.Add(product("a", price = 100.0, discount = 25.0)))
        cart.dispatch(CartAction.SetQuantity("a", 2))      // 2 x 75
        cart.dispatch(CartAction.Add(product("b", price = 10.0)))   // + 10

        assertEquals(160.0, cart.state.value.total, 0.0001)
    }

    @Test
    fun `clear empties the cart`() {
        val cart = CartViewModel()
        cart.dispatch(CartAction.Add(product("a")))
        cart.dispatch(CartAction.Add(product("b")))
        cart.dispatch(CartAction.Clear)

        assertTrue(cart.state.value.isEmpty)
        assertEquals(0.0, cart.state.value.total, 0.0001)
    }
}
