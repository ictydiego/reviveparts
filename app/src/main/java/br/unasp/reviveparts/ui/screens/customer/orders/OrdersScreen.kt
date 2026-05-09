package br.unasp.reviveparts.ui.screens.customer.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.ui.nav.Routes

@Composable
fun OrdersScreen(nav: NavController) {
    val ctx = LocalContext.current
    val vm: OrdersViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = OrdersViewModel.create(ctx.app) as T
    })
    val items by vm.items.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Meus pedidos", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        if (items.isEmpty()) Text("Nenhum pedido ainda.")
        LazyColumn {
            items(items, key = { it.id }) { o ->
                Card(onClick = { nav.navigate(Routes.orderDetail(o.id)) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Pedido #${o.id}", style = MaterialTheme.typography.titleLarge)
                        Text("Status: ${o.status.label}", color = MaterialTheme.colorScheme.primary)
                        Text("R$ %.2f — ${o.paymentType.name}".format(o.totalCents / 100.0))
                    }
                }
            }
        }
    }
}
