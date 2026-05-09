package br.unasp.reviveparts.ui.nav

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "auth/login"
    const val REGISTER = "auth/register"

    const val CUSTOMER_HOME = "customer/home"
    const val CUSTOMER_AI = "customer/ai"
    const val CUSTOMER_ORDERS = "customer/orders"
    const val CUSTOMER_PROFILE = "customer/profile"
    fun partDetail(id: Long) = "customer/part/$id"
    const val PART_DETAIL = "customer/part/{id}"
    fun cart(productId: Long, source: String) = "customer/cart/$productId/$source"
    const val CART = "customer/cart/{productId}/{source}"
    fun payment(productId: Long, source: String) = "customer/payment/$productId/$source"
    const val PAYMENT = "customer/payment/{productId}/{source}"
    fun orderDetail(id: Long) = "customer/order/$id"
    const val ORDER_DETAIL = "customer/order/{id}"

    const val OWNER_DASHBOARD = "owner/dashboard"
    const val OWNER_PRODUCTS = "owner/products"
    const val OWNER_PROFILE = "owner/profile"
    fun ownerOrderDetail(id: Long) = "owner/order/$id"
    const val OWNER_ORDER_DETAIL = "owner/order/{id}"
    fun productEdit(id: Long?) = if (id == null) "owner/product/edit" else "owner/product/edit/$id"
    const val PRODUCT_EDIT = "owner/product/edit/{id}"
    const val PRODUCT_NEW = "owner/product/edit"
}
