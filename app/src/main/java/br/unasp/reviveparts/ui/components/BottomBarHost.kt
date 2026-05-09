package br.unasp.reviveparts.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import br.unasp.reviveparts.domain.model.Role
import br.unasp.reviveparts.ui.nav.Routes
import br.unasp.reviveparts.ui.theme.YellowPrimary

data class TabItem(val route: String, val label: String, val icon: ImageVector)

private val customerTabs = listOf(
    TabItem(Routes.CUSTOMER_HOME, "Início", Icons.Default.Home),
    TabItem(Routes.CUSTOMER_AI, "Buscar", Icons.Default.AddCircle),
    TabItem(Routes.CUSTOMER_ORDERS, "Pedidos", Icons.Default.ShoppingBag)
)
private val ownerTabs = listOf(
    TabItem(Routes.OWNER_DASHBOARD, "Pedidos", Icons.Default.Receipt),
    TabItem(Routes.OWNER_PRODUCTS, "Produtos", Icons.Default.Inventory),
    TabItem(Routes.OWNER_PROFILE, "Perfil", Icons.Default.Person)
)

@Composable
fun AppBottomBar(nav: NavController, role: Role) {
    val tabs = if (role == Role.OWNER) ownerTabs else customerTabs
    val current = nav.currentBackStackEntryAsState().value?.destination?.route
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        tabs.forEach { t ->
            NavigationBarItem(
                selected = current == t.route,
                onClick = { nav.navigate(t.route) { launchSingleTop = true; popUpTo(nav.graph.startDestinationId) } },
                icon = { Icon(t.icon, t.label) },
                label = { Text(t.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = YellowPrimary,
                    selectedTextColor = YellowPrimary,
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}
