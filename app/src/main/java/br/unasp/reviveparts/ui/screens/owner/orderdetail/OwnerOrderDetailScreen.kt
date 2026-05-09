package br.unasp.reviveparts.ui.screens.owner.orderdetail

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
import br.unasp.reviveparts.domain.model.OrderStatus
import br.unasp.reviveparts.ui.components.StatusStepper
import br.unasp.reviveparts.ui.components.YellowButton

@Composable
fun OwnerOrderDetailScreen(nav: NavController, id: Long) {
    val ctx = LocalContext.current
    val vm: OwnerOrderDetailViewModel = viewModel(key = "ood-$id", factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = OwnerOrderDetailViewModel.create(ctx.app, id) as T
    })
    val o by vm.order.collectAsState()
    val p by vm.product.collectAsState()
    val u by vm.customer.collectAsState()
    val order = o ?: return

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Pedido #${order.id}", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        u?.let { Text("Cliente: ${it.name} (${it.email})"); Text("Endereço: ${it.address}"); Text("Tel: ${it.phone}") }
        Spacer(Modifier.height(8.dp))
        p?.let { Text("Peça: ${it.name}", style = MaterialTheme.typography.titleLarge) }
        Text("Total: R$ %.2f".format(order.totalCents / 100.0))
        Text("Pagamento: ${order.paymentType.name}")
        Text("Origem: ${order.source.name}")
        Spacer(Modifier.height(16.dp))
        StatusStepper(order.status)
        Spacer(Modifier.height(16.dp))
        val isDone = order.status == OrderStatus.DELIVERED
        YellowButton(
            if (isDone) "Concluído" else "Avançar para: ${order.status.next()?.label ?: "—"}",
            { vm.advance() },
            Modifier.fillMaxWidth(),
            enabled = !isDone
        )
    }
}
