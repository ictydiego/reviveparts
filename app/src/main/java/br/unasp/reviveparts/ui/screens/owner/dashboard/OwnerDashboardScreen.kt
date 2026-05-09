package br.unasp.reviveparts.ui.screens.owner.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.data.db.entities.OrderEntity
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.domain.model.OrderStatus
import br.unasp.reviveparts.ui.nav.Routes
import br.unasp.reviveparts.ui.screens.customer.formatDateShort
import br.unasp.reviveparts.ui.screens.customer.formatOrderId
import br.unasp.reviveparts.ui.screens.customer.productImage
import br.unasp.reviveparts.ui.theme.Black0
import br.unasp.reviveparts.ui.theme.YellowPrimary

@Composable
fun OwnerDashboardScreen(nav: NavController) {
    val ctx = LocalContext.current
    val vm: OwnerDashboardViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = OwnerDashboardViewModel.create(ctx.app) as T
    })
    val sel by vm.selected.collectAsState()
    val orders by vm.list.collectAsState()
    val all by vm.all.collectAsState()
    val products by vm.productMap.collectAsState()

    val producing = all.count { it.status == OrderStatus.PRINTING || it.status == OrderStatus.PACKING }
    val pending = all.count { it.status == OrderStatus.PLACED || it.status == OrderStatus.IN_REVIEW }
    val revenueToday = all.filter { it.status == OrderStatus.DELIVERED }.sumOf { it.totalCents } / 100.0

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Painel", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("ReviveParts", style = MaterialTheme.typography.headlineMedium, color = YellowPrimary)
            }
            Surface(shape = CircleShape, color = YellowPrimary, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AdminPanelSettings, null, tint = Black0)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Pendentes", "$pending", Icons.Default.HourglassTop, modifier = Modifier.weight(1f))
            StatCard("Produzindo", "$producing", Icons.Default.Build, modifier = Modifier.weight(1f))
            StatCard("Total", "${all.size}", Icons.Default.Receipt, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = YellowPrimary, contentColor = Black0),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AttachMoney, null, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text("Faturado (entregues)", style = MaterialTheme.typography.labelLarge)
                    Text("R$ %.2f".format(revenueToday), style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Pedidos por status", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            OrderStatus.pipeline.forEach { s ->
                val count = all.count { it.status == s }
                StatusChip(s.label, count, sel == s) { vm.select(s) }
                Spacer(Modifier.width(6.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        if (orders.isEmpty()) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Inbox, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Nenhum pedido em ${sel.label}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(orders, key = { it.id }) { o ->
                OrderCard(o, products[o.productId]) { nav.navigate(Routes.ownerOrderDetail(o.id)) }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(Modifier.padding(12.dp)) {
            Icon(icon, null, tint = YellowPrimary)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatusChip(label: String, count: Int, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) YellowPrimary else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) Black0 else MaterialTheme.colorScheme.onSurface
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(6.dp))
            Surface(
                shape = CircleShape,
                color = if (selected) Black0 else YellowPrimary.copy(alpha = 0.3f),
                contentColor = if (selected) YellowPrimary else MaterialTheme.colorScheme.onSurface
            ) {
                Text("$count", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }
    }
}

@Composable
private fun OrderCard(order: OrderEntity, product: ProductEntity?, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
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
                Text(product?.name ?: "—", style = MaterialTheme.typography.titleLarge, maxLines = 1)
                Text(
                    "#${formatOrderId(order.id)}  •  ${formatDateShort(order.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "R$ %.2f".format(order.totalCents / 100.0),
                    style = MaterialTheme.typography.titleLarge,
                    color = YellowPrimary
                )
            }
            Icon(Icons.Default.ChevronRight, null, tint = YellowPrimary)
        }
    }
}
