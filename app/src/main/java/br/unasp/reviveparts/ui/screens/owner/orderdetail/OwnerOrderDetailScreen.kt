package br.unasp.reviveparts.ui.screens.owner.orderdetail

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.domain.model.OrderStatus
import br.unasp.reviveparts.ui.screens.customer.display
import br.unasp.reviveparts.ui.screens.customer.formatOrderId
import br.unasp.reviveparts.ui.screens.customer.productImage
import br.unasp.reviveparts.ui.theme.Black0
import br.unasp.reviveparts.ui.theme.YellowPrimary

@Composable
fun OwnerOrderDetailScreen(nav: NavController, id: Long) {
    val ctx = LocalContext.current
    val vm: OwnerOrderDetailViewModel = viewModel(key = "ood-$id", factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = OwnerOrderDetailViewModel.create(ctx.app, id) as T
    })
    val o by vm.order.collectAsState()
    val p by vm.product.collectAsState()
    val u by vm.customer.collectAsState()
    val order = o ?: return
    val display = order.status.display()
    val isDone = order.status == OrderStatus.DELIVERED

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = YellowPrimary) }
                Column(Modifier.weight(1f)) {
                    Text("Pedido #${formatOrderId(order.id)}", style = MaterialTheme.typography.titleLarge)
                }
                Surface(shape = RoundedCornerShape(20.dp), color = YellowPrimary.copy(alpha = 0.2f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(display.emoji); Spacer(Modifier.width(4.dp))
                        Text(display.label, color = YellowPrimary, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(72.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (p != null) Image(
                            painterResource(productImage(p!!.name)),
                            null,
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(p?.name ?: "—", style = MaterialTheme.typography.titleLarge)
                        Text("Origem: ${order.source.name}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text("R$ %.2f".format(order.totalCents / 100.0), style = MaterialTheme.typography.headlineMedium, color = YellowPrimary)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Cliente", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    InfoLine(Icons.Default.Person, "Nome", u?.name ?: "—")
                    InfoLine(Icons.Default.Email, "E-mail", u?.email ?: "—")
                    InfoLine(Icons.Default.Phone, "Telefone", u?.phone?.takeIf { it.isNotBlank() } ?: "—")
                    InfoLine(Icons.Default.LocationOn, "Endereço", u?.address?.takeIf { it.isNotBlank() } ?: "—")
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Pagamento", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp).clip(CircleShape).background(YellowPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (order.paymentType.name == "CARD") Icons.Default.CreditCard else Icons.Default.QrCode2,
                            null,
                            tint = YellowPrimary
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(order.paymentType.name, modifier = Modifier.weight(1f))
                    Text("Confirmado", color = YellowPrimary, style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Pipeline", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            PipelineList(order.status)
            Spacer(Modifier.height(120.dp))
        }

        Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { vm.advance() },
                enabled = !isDone,
                colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary, contentColor = Black0),
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                if (isDone) {
                    Icon(Icons.Default.CheckCircle, null); Spacer(Modifier.width(8.dp)); Text("Concluído", style = MaterialTheme.typography.titleLarge)
                } else {
                    Icon(Icons.Default.ArrowForward, null); Spacer(Modifier.width(8.dp))
                    Text("Avançar para ${order.status.next()?.label ?: "—"}", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

@Composable
private fun InfoLine(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Icon(icon, null, tint = YellowPrimary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun PipelineList(current: OrderStatus) {
    Column {
        OrderStatus.pipeline.forEachIndexed { i, s ->
            val done = s.ordinal < current.ordinal
            val active = s == current
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
                Text(
                    s.label,
                    style = if (active) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                    color = if (active || done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (i < OrderStatus.pipeline.lastIndex) {
                Box(Modifier.padding(start = 13.dp).width(2.dp).height(16.dp).background(MaterialTheme.colorScheme.outline))
            }
        }
    }
}
