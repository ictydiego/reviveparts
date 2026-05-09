package br.unasp.reviveparts.ui.screens.owner.products

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.ui.nav.Routes
import br.unasp.reviveparts.ui.screens.customer.productImage
import br.unasp.reviveparts.ui.theme.Black0
import br.unasp.reviveparts.ui.theme.YellowPrimary

@Composable
fun ProductsScreen(nav: NavController) {
    val ctx = LocalContext.current
    val vm: ProductsViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = ProductsViewModel.create(ctx.app) as T
    })
    val items by vm.items.collectAsState(initial = emptyList())
    val totalStock = items.sumOf { it.stockQty }
    val ready = items.count { it.isReady }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { nav.navigate(Routes.PRODUCT_NEW) },
                containerColor = YellowPrimary,
                contentColor = Black0
            ) {
                Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Novo")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(16.dp))
            Text("Produtos", style = MaterialTheme.typography.headlineMedium)
            Text("Gerencie o catálogo da loja", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniStat("${items.size}", "produtos", Modifier.weight(1f))
                MiniStat("$ready", "disponíveis", Modifier.weight(1f))
                MiniStat("$totalStock", "em estoque", Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
            if (items.isEmpty()) EmptyState()
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items, key = { it.id }) { p ->
                    ProductRow(p, onEdit = { nav.navigate(Routes.productEdit(p.id)) }, onDelete = { vm.delete(p) })
                }
                item { Spacer(Modifier.height(96.dp)) }
            }
        }
    }
}

@Composable
private fun MiniStat(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = YellowPrimary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyState() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Inventory2, null, tint = YellowPrimary, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(8.dp))
            Text("Nenhum produto cadastrado", style = MaterialTheme.typography.titleLarge)
            Text("Use o + para adicionar um", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProductRow(p: ProductEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        onClick = onEdit,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painterResource(productImage(p.name)),
                    null,
                    modifier = Modifier.fillMaxSize().padding(6.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(p.name, style = MaterialTheme.typography.titleLarge, maxLines = 1)
                Text("R$ %.2f".format(p.priceCents / 100.0), style = MaterialTheme.typography.titleLarge, color = YellowPrimary)
                Row(modifier = Modifier.padding(top = 2.dp)) {
                    StockChip(p.stockQty)
                    Spacer(Modifier.width(6.dp))
                    HoursChip(p.prototypeHours)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, "excluir", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun StockChip(qty: Int) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (qty > 0) YellowPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            if (qty > 0) "$qty em estoque" else "Esgotado",
            style = MaterialTheme.typography.labelSmall,
            color = if (qty > 0) YellowPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun HoursChip(h: Int) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            "${h}h prototipagem",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
