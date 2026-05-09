package br.unasp.reviveparts.ui.screens.owner.productedit

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.ui.components.PrimaryTextField
import br.unasp.reviveparts.ui.screens.customer.productImage
import br.unasp.reviveparts.ui.theme.Black0
import br.unasp.reviveparts.ui.theme.YellowPrimary

@Composable
fun ProductEditScreen(nav: NavController, id: Long?) {
    val ctx = LocalContext.current
    val vm: ProductEditViewModel = viewModel(key = "pe-${id ?: "new"}", factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = ProductEditViewModel.create(ctx.app, id) as T
    })
    val p by vm.product.collectAsState()

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = YellowPrimary) }
                Text(if (id == null) "Novo produto" else "Editar produto", style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.height(12.dp))

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
                        Image(
                            painterResource(productImage(p.name)),
                            null,
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(p.name.ifBlank { "Nome do produto" }, style = MaterialTheme.typography.titleLarge)
                        Text("R$ %.2f".format(p.priceCents / 100.0), style = MaterialTheme.typography.headlineMedium, color = YellowPrimary)
                        Text(
                            if (p.stockQty > 0) "${p.stockQty} em estoque" else "${p.prototypeHours}h prototipagem",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionLabel("Informações básicas")
            FormCard {
                PrimaryTextField(p.name, { v -> vm.update { it.copy(name = v) } }, "Nome")
                Spacer(Modifier.height(8.dp))
                PrimaryTextField(p.description, { v -> vm.update { it.copy(description = v) } }, "Descrição")
            }

            Spacer(Modifier.height(16.dp))
            SectionLabel("Preço & produção")
            FormCard {
                PrimaryTextField(
                    (p.priceCents / 100.0).toString(),
                    { v -> vm.update { it.copy(priceCents = ((v.toDoubleOrNull() ?: 0.0) * 100).toLong()) } },
                    "Preço (R$)",
                    keyboardType = KeyboardType.Decimal
                )
                Spacer(Modifier.height(8.dp))
                Row {
                    PrimaryTextField(
                        p.prototypeHours.toString(),
                        { v -> vm.update { it.copy(prototypeHours = v.toIntOrNull() ?: 0) } },
                        "Horas prototipagem",
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Number
                    )
                    Spacer(Modifier.width(8.dp))
                    PrimaryTextField(
                        p.stockQty.toString(),
                        { v -> vm.update { it.copy(stockQty = v.toIntOrNull() ?: 0) } },
                        "Estoque",
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Number
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionLabel("Disponibilidade")
            FormCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Pronta entrega", style = MaterialTheme.typography.titleLarge)
                        Text(
                            if (p.isReady) "Aparece no catálogo" else "Apenas sob encomenda",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = p.isReady, onCheckedChange = { v -> vm.update { it.copy(isReady = v) } })
                }
            }
            Spacer(Modifier.height(120.dp))
        }

        Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { vm.save { nav.popBackStack() } },
                colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary, contentColor = Black0),
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Check, null); Spacer(Modifier.width(8.dp)); Text("Salvar produto", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = YellowPrimary,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun FormCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}
