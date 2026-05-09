package br.unasp.reviveparts.ui.screens.owner.productedit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.ui.components.PrimaryTextField
import br.unasp.reviveparts.ui.components.YellowButton

@Composable
fun ProductEditScreen(nav: NavController, id: Long?) {
    val ctx = LocalContext.current
    val vm: ProductEditViewModel = viewModel(key = "pe-${id ?: "new"}", factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = ProductEditViewModel.create(ctx.app, id) as T
    })
    val p by vm.product.collectAsState()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text(if (id == null) "Novo produto" else "Editar produto", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        PrimaryTextField(p.name, { v -> vm.update { it.copy(name = v) } }, "Nome")
        Spacer(Modifier.height(8.dp))
        PrimaryTextField(p.description, { v -> vm.update { it.copy(description = v) } }, "Descrição")
        Spacer(Modifier.height(8.dp))
        PrimaryTextField((p.priceCents / 100.0).toString(), { v -> vm.update { it.copy(priceCents = ((v.toDoubleOrNull() ?: 0.0) * 100).toLong()) } }, "Preço (R$)", keyboardType = KeyboardType.Decimal)
        Spacer(Modifier.height(8.dp))
        PrimaryTextField(p.prototypeHours.toString(), { v -> vm.update { it.copy(prototypeHours = v.toIntOrNull() ?: 0) } }, "Horas de prototipagem", keyboardType = KeyboardType.Number)
        Spacer(Modifier.height(8.dp))
        PrimaryTextField(p.stockQty.toString(), { v -> vm.update { it.copy(stockQty = v.toIntOrNull() ?: 0) } }, "Estoque", keyboardType = KeyboardType.Number)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Switch(checked = p.isReady, onCheckedChange = { v -> vm.update { it.copy(isReady = v) } })
            Spacer(Modifier.width(8.dp)); Text("Pronta entrega")
        }
        Spacer(Modifier.height(16.dp))
        YellowButton("Salvar", { vm.save { nav.popBackStack() } }, Modifier.fillMaxWidth())
    }
}
