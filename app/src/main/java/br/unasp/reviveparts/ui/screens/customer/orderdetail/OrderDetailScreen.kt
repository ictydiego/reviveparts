package br.unasp.reviveparts.ui.screens.customer.orderdetail

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.domain.model.OrderStatus
import br.unasp.reviveparts.ui.components.LivePrinterDialog
import br.unasp.reviveparts.ui.screens.customer.customerStageLabels
import br.unasp.reviveparts.ui.screens.customer.display
import br.unasp.reviveparts.ui.screens.customer.formatDateShort
import br.unasp.reviveparts.ui.screens.customer.formatOrderId
import br.unasp.reviveparts.ui.screens.customer.productImage
import br.unasp.reviveparts.ui.theme.Black0
import br.unasp.reviveparts.ui.theme.YellowPrimary

@Composable
fun OrderDetailScreen(nav: NavController, id: Long) {
    val ctx = LocalContext.current
    val vm: OrderDetailViewModel = viewModel(key = "od-$id", factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = OrderDetailViewModel.create(ctx.app, id) as T
    })
    val o by vm.order.collectAsState()
    val p by vm.product.collectAsState()
    val order = o ?: return
    val product = p
    val display = order.status.display()
    var rating by remember { mutableIntStateOf(0) }
    var liveOpen by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = YellowPrimary) }
            Text("Pedido #${formatOrderId(order.id)}", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (product != null) Image(
                        painterResource(productImage(product.name)),
                        null,
                        modifier = Modifier.fillMaxSize().padding(8.dp),
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
                    Spacer(Modifier.height(4.dp))
                    Text("R$ %.2f".format(order.totalCents / 100.0), style = MaterialTheme.typography.headlineMedium, color = YellowPrimary)
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                SpecRow("Material", "Plástico ABS")
                Divider()
                SpecRow("Peso estimado", "~ 140g")
                Divider()
                SpecRow("Previsão de entrega", if (order.status == OrderStatus.DELIVERED) "Entregue ${formatDateShort(order.createdAt)}" else "06 Mai - 08 Mai")
                Divider()
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                    Text("Status", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    StatusBadge(emoji = display.emoji, label = display.label)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Acompanhar pedido", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        StageList(currentStage = display.stage, onLiveClick = { liveOpen = true })

        if (order.status == OrderStatus.DELIVERED) {
            Spacer(Modifier.height(24.dp))
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Avalie este produto", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Sua opinião é muito importante!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Row {
                        repeat(5) { i ->
                            IconButton(onClick = { rating = i + 1 }) {
                                Icon(
                                    if (i < rating) Icons.Default.Star else Icons.Default.StarBorder,
                                    null,
                                    tint = YellowPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { /* mock submit */ },
                        enabled = rating > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary, contentColor = Black0),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Enviar a avaliação") }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    if (liveOpen) {
        LivePrinterDialog(orderId = order.id, onDismiss = { liveOpen = false })
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
}

@Composable
private fun StatusBadge(emoji: String, label: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = YellowPrimary.copy(alpha = 0.2f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(emoji)
            Spacer(Modifier.width(4.dp))
            Text(label, color = YellowPrimary, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun StageList(currentStage: Int, onLiveClick: () -> Unit) {
    Column {
        customerStageLabels.forEachIndexed { i, label ->
            val done = i < currentStage
            val active = i == currentStage
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Box(
                    Modifier.size(28.dp).clip(CircleShape)
                        .background(if (done || active) YellowPrimary else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        done -> Icon(Icons.Default.Check, null, tint = Black0, modifier = Modifier.size(18.dp))
                        active -> Icon(Icons.Default.HourglassTop, null, tint = Black0, modifier = Modifier.size(16.dp))
                        else -> Icon(Icons.Default.Circle, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(8.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(label, color = if (active || done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                if (i == 1 && active) {
                    LiveButton(onClick = onLiveClick)
                }
            }
            if (i < customerStageLabels.lastIndex) {
                Box(Modifier.padding(start = 13.dp).width(2.dp).height(16.dp).background(MaterialTheme.colorScheme.outline))
            }
        }
    }
}

@Composable
private fun LiveButton(onClick: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "btn-pulse")
    val alpha by infinite.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "alpha"
    )
    Surface(
        onClick = onClick,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        color = androidx.compose.ui.graphics.Color(0xFFE53935)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Box(
                Modifier.size(8.dp).clip(CircleShape)
                    .background(androidx.compose.ui.graphics.Color.White.copy(alpha = alpha))
            )
            Spacer(Modifier.width(6.dp))
            Text("LIVE", color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.labelLarge)
        }
    }
}
