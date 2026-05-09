package br.unasp.reviveparts.ui.screens.customer.partdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.domain.model.OrderSource
import br.unasp.reviveparts.ui.components.ModelViewer3D
import br.unasp.reviveparts.ui.nav.Routes
import br.unasp.reviveparts.ui.theme.Black0
import br.unasp.reviveparts.ui.theme.YellowPrimary

@Composable
fun PartDetailScreen(nav: NavController, id: Long) {
    val ctx = LocalContext.current
    val vm: PartDetailViewModel = viewModel(key = "part-$id", factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = PartDetailViewModel.create(ctx.app, id) as T
    })
    val p by vm.product.collectAsState()
    val product = p ?: return
    var liked by remember { mutableStateOf(false) }
    var qty by remember { mutableIntStateOf(1) }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { nav.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, "voltar", tint = YellowPrimary)
                }
                Text("Detalhes da peça", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                IconButton(onClick = { liked = !liked }) {
                    Icon(
                        if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        null,
                        tint = if (liked) YellowPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                ModelViewer3D(
                    assetPath = product.model3dAsset,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    color = Black0.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Refresh, null, tint = YellowPrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Arraste para girar", style = MaterialTheme.typography.labelSmall, color = YellowPrimary)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(product.name, style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, null, tint = YellowPrimary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("5.0", style = MaterialTheme.typography.labelLarge)
                Text("  •  142 vendidos", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(12.dp))
            ChipsRow(product)
            Spacer(Modifier.height(16.dp))
            Text("Descrição", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(product.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(20.dp))
            Text("Especificações", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            SpecGrid()

            Spacer(Modifier.height(20.dp))
            Text("Compatibilidade", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            CompatibilityList()
            Spacer(Modifier.height(120.dp))
        }

        BuyBar(
            product = product,
            qty = qty,
            onChangeQty = { qty = (qty + it).coerceAtLeast(1) },
            onBuy = { nav.navigate(Routes.cart(product.id, OrderSource.CATALOG.name)) }
        )
    }
}

@Composable
private fun ChipsRow(p: ProductEntity) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (p.stockQty > 0) {
            InfoChip(Icons.Default.Inventory2, "Em estoque (${p.stockQty})", YellowPrimary, Black0)
        } else {
            InfoChip(Icons.Default.Schedule, "${p.prototypeHours}h prototipagem", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface)
        }
        InfoChip(Icons.Default.LocalShipping, "Entrega 3-5 dias", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun InfoChip(icon: ImageVector, label: String, bg: androidx.compose.ui.graphics.Color, fg: androidx.compose.ui.graphics.Color) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bg,
        contentColor = fg
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun SpecGrid() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            SpecItem("Material", "Plástico ABS reforçado")
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            SpecItem("Peso estimado", "~ 140g")
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            SpecItem("Dimensões", "13 - 15 cm")
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            SpecItem("Acabamento", "Camada 0.2mm, polido")
        }
    }
}

@Composable
private fun SpecItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun CompatibilityList() {
    val items = listOf("VW Fusca 1965-1996", "VW Kombi 1957-2013", "VW Brasília 1973-1982")
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            items.forEachIndexed { i, txt ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                    Box(
                        Modifier.size(28.dp).clip(CircleShape).background(YellowPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.DirectionsCar, null, tint = YellowPrimary, modifier = Modifier.size(16.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Text(txt, style = MaterialTheme.typography.bodyLarge)
                }
                if (i < items.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
private fun BuyBar(product: ProductEntity, qty: Int, onChangeQty: (Int) -> Unit, onBuy: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "R$ %.2f".format(product.priceCents * qty / 100.0),
                        color = YellowPrimary,
                        style = MaterialTheme.typography.displayLarge
                    )
                }
                QtyStepper(qty, onChangeQty)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onBuy,
                colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary, contentColor = Black0),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.ShoppingCart, null)
                Spacer(Modifier.width(8.dp))
                Text("Comprar agora", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun QtyStepper(qty: Int, onChange: (Int) -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
            IconButton(onClick = { onChange(-1) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Remove, "menos", tint = YellowPrimary)
            }
            Text("$qty", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 8.dp))
            IconButton(onClick = { onChange(1) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Add, "mais", tint = YellowPrimary)
            }
        }
    }
}
