package br.unasp.reviveparts.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import br.unasp.reviveparts.app
import br.unasp.reviveparts.domain.model.Role
import br.unasp.reviveparts.ui.components.AppBottomBar
import br.unasp.reviveparts.ui.screens.auth.LoginScreen
import br.unasp.reviveparts.ui.screens.auth.RegisterScreen
import br.unasp.reviveparts.ui.screens.customer.ai.AiSearchScreen
import br.unasp.reviveparts.ui.screens.customer.cart.CartScreen
import br.unasp.reviveparts.ui.screens.customer.home.HomeScreen
import br.unasp.reviveparts.ui.screens.customer.orderdetail.OrderDetailScreen
import br.unasp.reviveparts.ui.screens.customer.orders.OrdersScreen
import br.unasp.reviveparts.ui.screens.customer.partdetail.PartDetailScreen
import br.unasp.reviveparts.ui.screens.customer.payment.PaymentScreen
import br.unasp.reviveparts.ui.screens.customer.profile.ProfileScreen
import br.unasp.reviveparts.ui.screens.owner.dashboard.OwnerDashboardScreen
import br.unasp.reviveparts.ui.screens.owner.orderdetail.OwnerOrderDetailScreen
import br.unasp.reviveparts.ui.screens.owner.productedit.ProductEditScreen
import br.unasp.reviveparts.ui.screens.owner.products.ProductsScreen
import br.unasp.reviveparts.ui.screens.owner.profile.OwnerProfileScreen
import androidx.compose.ui.platform.LocalContext

@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    val ctx = LocalContext.current
    val sessionFlow = ctx.app.sessionRepo.session
    val session by sessionFlow.collectAsState(initial = null)
    val sessionLoaded = remember { mutableStateOf(false) }
    LaunchedEffect(session) { sessionLoaded.value = true }

    val role = session?.role
    val showBar = role != null && nav.currentBackStackEntryAsState().value?.destination?.route in withBarRoutes

    Scaffold(
        bottomBar = { if (showBar && role != null) AppBottomBar(nav, role) }
    ) { pad ->
        NavHost(
            navController = nav,
            startDestination = if (session == null) Routes.LOGIN else when (role) {
                Role.OWNER -> Routes.OWNER_DASHBOARD
                else -> Routes.CUSTOMER_HOME
            },
            modifier = Modifier.padding(pad)
        ) {
            composable(Routes.LOGIN) { LoginScreen(nav) }
            composable(Routes.REGISTER) { RegisterScreen(nav) }

            composable(Routes.CUSTOMER_HOME) { HomeScreen(nav) }
            composable(Routes.CUSTOMER_AI) { AiSearchScreen(nav) }
            composable(Routes.CUSTOMER_ORDERS) { OrdersScreen(nav) }
            composable(Routes.CUSTOMER_PROFILE) { ProfileScreen(nav) }
            composable(Routes.PART_DETAIL, listOf(navArgument("id") { type = NavType.LongType })) {
                PartDetailScreen(nav, it.arguments!!.getLong("id"))
            }
            composable(
                Routes.CART,
                listOf(
                    navArgument("productId") { type = NavType.LongType },
                    navArgument("source") { type = NavType.StringType }
                )
            ) {
                CartScreen(nav, it.arguments!!.getLong("productId"), it.arguments!!.getString("source")!!)
            }
            composable(
                Routes.PAYMENT,
                listOf(
                    navArgument("productId") { type = NavType.LongType },
                    navArgument("source") { type = NavType.StringType }
                )
            ) {
                PaymentScreen(nav, it.arguments!!.getLong("productId"), it.arguments!!.getString("source")!!)
            }
            composable(Routes.ORDER_DETAIL, listOf(navArgument("id") { type = NavType.LongType })) {
                OrderDetailScreen(nav, it.arguments!!.getLong("id"))
            }

            composable(Routes.OWNER_DASHBOARD) { OwnerDashboardScreen(nav) }
            composable(Routes.OWNER_PRODUCTS) { ProductsScreen(nav) }
            composable(Routes.OWNER_PROFILE) { OwnerProfileScreen(nav) }
            composable(Routes.OWNER_ORDER_DETAIL, listOf(navArgument("id") { type = NavType.LongType })) {
                OwnerOrderDetailScreen(nav, it.arguments!!.getLong("id"))
            }
            composable(Routes.PRODUCT_NEW) { ProductEditScreen(nav, null) }
            composable(Routes.PRODUCT_EDIT, listOf(navArgument("id") { type = NavType.LongType })) {
                ProductEditScreen(nav, it.arguments!!.getLong("id"))
            }
        }
    }
}

private val withBarRoutes = setOf(
    Routes.CUSTOMER_HOME, Routes.CUSTOMER_AI, Routes.CUSTOMER_ORDERS, Routes.CUSTOMER_PROFILE,
    Routes.OWNER_DASHBOARD, Routes.OWNER_PRODUCTS, Routes.OWNER_PROFILE
)
