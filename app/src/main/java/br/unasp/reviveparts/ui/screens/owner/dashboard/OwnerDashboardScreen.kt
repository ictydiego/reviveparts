package br.unasp.reviveparts.ui.screens.owner.dashboard

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.domain.model.OrderStatus
import br.unasp.reviveparts.ui.nav.Routes

@Composable
fun OwnerDashboardScreen(nav: NavController) {
    val ctx = LocalContext.current
    val vm: OwnerDashboardViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = OwnerDashboardViewModel.create(ctx.app) as T
    })
    val sel by vm.selected.collectAsState()
    val orders by vm.list.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Pedidos", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            OrderStatus.pipeline.forEach { s ->
                FilterChip(
                    selected = sel == s,
                    onClick = { vm.select(s) },
                    label = { Text(s.label) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        if (orders.isEmpty()) Text("Nenhum pedido em ${sel.label}.")
        LazyColumn {
            items(orders, key = { it.id }) { o ->
                Card(onClick = { nav.navigate(Routes.ownerOrderDetail(o.id)) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Pedido #${o.id} — usuário ${o.userId}")
                        Text("R$ %.2f  •  ${o.paymentType.name}".format(o.totalCents / 100.0))
                    }
                }
            }
        }
    }
}
