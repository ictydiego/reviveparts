package br.unasp.reviveparts.ui.screens.customer.orders

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.data.db.entities.OrderEntity
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.ui.nav.Routes
import br.unasp.reviveparts.ui.screens.customer.customerStageShort
import br.unasp.reviveparts.ui.screens.customer.display
import br.unasp.reviveparts.ui.screens.customer.formatDateShort
import br.unasp.reviveparts.ui.screens.customer.formatOrderId
import br.unasp.reviveparts.ui.screens.customer.productImage
import br.unasp.reviveparts.ui.theme.YellowPrimary

@Composable
fun OrdersScreen(nav: NavController) {
    val ctx = LocalContext.current
    val vm: OrdersViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = OrdersViewModel.create(ctx.app) as T
    })
    val items by vm.items.collectAsState()
    val products by vm.products.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = YellowPrimary) }
            Text("Meus pedidos", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(8.dp))
        if (items.isEmpty()) {
            Text("Nenhum pedido ainda.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items, key = { it.id }) { o ->
                OrderRow(o, products[o.productId]) { nav.navigate(Routes.orderDetail(o.id)) }
            }
        }
    }
}

@Composable
private fun OrderRow(order: OrderEntity, product: ProductEntity?, onClick: () -> Unit) {
    val display = order.status.display()
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (product != null) Image(
                        painterResource(productImage(product.name)),
                        null,
                        modifier = Modifier.fillMaxSize().padding(6.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(product?.name ?: "—", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${formatDateShort(order.createdAt)}  #${formatOrderId(order.id)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "R$ %.2f".format(order.totalCents / 100.0),
                    style = MaterialTheme.typography.headlineMedium,
                    color = YellowPrimary,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = YellowPrimary.copy(alpha = 0.2f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(display.emoji); Spacer(Modifier.width(4.dp))
                        Text(display.label, color = YellowPrimary, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            DotsProgress(currentStage = display.stage)
        }
    }
}

@Composable
private fun DotsProgress(currentStage: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        customerStageShort.forEachIndexed { i, label ->
            val done = i <= currentStage
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(
                    Modifier.size(14.dp).clip(CircleShape)
                        .background(if (done) YellowPrimary else MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(Modifier.height(4.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = if (done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (i < customerStageShort.lastIndex) {
                Box(
                    Modifier.weight(0.4f).height(2.dp)
                        .background(if (i < currentStage) YellowPrimary else MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
    }
}
