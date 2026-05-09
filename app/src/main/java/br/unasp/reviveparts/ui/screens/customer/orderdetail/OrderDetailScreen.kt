package br.unasp.reviveparts.ui.screens.customer.orderdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.ui.components.StatusStepper

@Composable
fun OrderDetailScreen(nav: NavController, id: Long) {
    val ctx = LocalContext.current
    val vm: OrderDetailViewModel = viewModel(key = "od-$id", factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = OrderDetailViewModel.create(ctx.app, id) as T
    })
    val o by vm.order.collectAsState()
    val p by vm.product.collectAsState()
    val order = o ?: return

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Pedido #${order.id}", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        p?.let { Text(it.name, style = MaterialTheme.typography.titleLarge) }
        Text("Total: R$ %.2f".format(order.totalCents / 100.0), color = MaterialTheme.colorScheme.primary)
        Text("Pagamento: ${order.paymentType.name}")
        Spacer(Modifier.height(24.dp))
        Text("Status", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        StatusStepper(order.status)
    }
}
