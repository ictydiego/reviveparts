package br.unasp.reviveparts.ui.screens.customer.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import br.unasp.reviveparts.R
import br.unasp.reviveparts.app
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.ui.nav.Routes
import br.unasp.reviveparts.ui.theme.Black0
import br.unasp.reviveparts.ui.theme.YellowPrimary

@Composable
fun CartScreen(nav: NavController, productId: Long, source: String) {
    val ctx = LocalContext.current
    val vm: CartViewModel = viewModel(key = "cart-$productId", factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = CartViewModel.create(ctx.app, productId) as T
    })
    val product by vm.product.collectAsState()
    val qty by vm.qty.collectAsState()
    val others by vm.others.collectAsState()
    val user by vm.user.collectAsState()
    val p = product ?: return
    val total = p.priceCents * qty

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = YellowPrimary) }
            Text("Confira o seu pedido", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(16.dp))

        SectionTitle("Entrega", "Alterar", Icons.Default.Edit) { }
        Spacer(Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, tint = YellowPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Estimativa: 3 - 5 dias úteis")
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.LocationOn, null, tint = YellowPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text(user?.address?.takeIf { it.isNotBlank() } ?: "Rua fictícia, 01, Jrd. Inventado - São Paulo/SP")
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionTitle("Resumo do pedido", "Limpar", Icons.Default.DeleteOutline) { nav.popBackStack() }
        Spacer(Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("$qty x ${p.name}", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "R$ %.2f".format(p.priceCents / 100.0),
                        style = MaterialTheme.typography.headlineMedium,
                        color = YellowPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.DeleteOutline, "remover") }
                    QtyStepper(qty, vm::changeQty)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { nav.popBackStack() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("+ Adicionar mais itens", color = YellowPrimary, style = MaterialTheme.typography.titleLarge)
        }

        Spacer(Modifier.height(16.dp))
        Text("Leve também", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            others.forEach { up ->
                UpsellCard(up, modifier = Modifier.weight(1f)) {
                    nav.navigate(Routes.cart(up.id, source)) { popUpTo(Routes.CUSTOMER_HOME) }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { nav.navigate(Routes.payment(p.id, source)) },
            colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary, contentColor = Black0),
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Continuar para pagamento", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowForward, null)
            Spacer(Modifier.width(12.dp))
            Text("R$ %.2f".format(total / 100.0), style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SectionTitle(title: String, actionLabel: String, actionIcon: androidx.compose.ui.graphics.vector.ImageVector, onAction: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        TextButton(onClick = onAction) {
            Icon(actionIcon, null, tint = YellowPrimary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text(actionLabel, color = YellowPrimary)
        }
    }
}

@Composable
private fun QtyStepper(qty: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SmallIconBtn(Icons.Default.Remove) { onChange(-1) }
        Text("$qty", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 12.dp))
        SmallIconBtn(Icons.Default.Add) { onChange(1) }
    }
}

@Composable
private fun SmallIconBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    FilledIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = YellowPrimary
        ),
        modifier = Modifier.size(36.dp)
    ) { Icon(icon, null) }
}

@Composable
private fun UpsellCard(p: ProductEntity, modifier: Modifier = Modifier, onAdd: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painterResource(upsellDrawable(p.name)),
                    null,
                    modifier = Modifier.fillMaxSize().padding(6.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(p.name, style = MaterialTheme.typography.labelSmall, maxLines = 1, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "R$ %.0f".format(p.priceCents / 100.0),
                    color = YellowPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f)
                )
                FilledIconButton(
                    onClick = onAdd,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = YellowPrimary, contentColor = Black0),
                    modifier = Modifier.size(28.dp)
                ) { Icon(Icons.Default.Add, "adicionar", modifier = Modifier.size(16.dp)) }
            }
        }
    }
}

private fun upsellDrawable(name: String): Int = br.unasp.reviveparts.ui.screens.customer.productImage(name)
