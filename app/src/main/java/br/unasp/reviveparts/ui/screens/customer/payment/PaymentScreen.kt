package br.unasp.reviveparts.ui.screens.customer.payment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.data.payments.PixGenerator
import br.unasp.reviveparts.domain.model.OrderSource
import br.unasp.reviveparts.ui.components.PrimaryTextField
import br.unasp.reviveparts.ui.components.YellowButton
import br.unasp.reviveparts.ui.nav.Routes

@Composable
fun PaymentScreen(nav: NavController, productId: Long, source: String) {
    val ctx = LocalContext.current
    val vm: PaymentViewModel = viewModel(key = "pay-$productId-$source", factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = PaymentViewModel.create(ctx.app, productId, OrderSource.valueOf(source)) as T
    })
    val state by vm.state.collectAsState()
    val product = state.product ?: return
    var tab by remember { mutableStateOf(0) }
    var number by remember { mutableStateOf("") }
    var holder by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var save by remember { mutableStateOf(true) }

    LaunchedEffect(state.createdOrderId) {
        val id = state.createdOrderId
        if (id != null && state.pixCopyPaste == null) {
            nav.navigate(Routes.orderDetail(id)) { popUpTo(Routes.CUSTOMER_HOME) }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Pagamento — R$ %.2f".format(product.priceCents / 100.0), style = MaterialTheme.typography.headlineMedium)
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Cartão") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("PIX") })
        }
        Spacer(Modifier.height(16.dp))
        when (tab) {
            0 -> {
                if (state.cards.isNotEmpty()) {
                    Text("Cartões salvos:")
                    state.cards.forEach { c ->
                        OutlinedButton(onClick = {
                            vm.payCard("4111111111111111", c.holderName, c.expiry, c.brand, save = false)
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("${c.brand} •••• ${c.last4}  ${c.holderName}")
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    Divider(Modifier.padding(vertical = 12.dp))
                    Text("Ou use um novo cartão:")
                    Spacer(Modifier.height(8.dp))
                }
                PrimaryTextField(number, { number = it }, "Número do cartão", keyboardType = KeyboardType.Number)
                Spacer(Modifier.height(8.dp))
                PrimaryTextField(holder, { holder = it }, "Nome impresso")
                Spacer(Modifier.height(8.dp))
                Row {
                    PrimaryTextField(expiry, { expiry = it }, "Validade MM/AA", Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    PrimaryTextField(cvv, { cvv = it }, "CVV", Modifier.weight(1f), keyboardType = KeyboardType.Number)
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = save, onCheckedChange = { save = it }); Text("Salvar cartão")
                }
                if (state.error != null) Text(state.error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                YellowButton(if (state.processing) "Processando..." else "Pagar", {
                    vm.payCard(number, holder, expiry, brand = detectBrand(number), save = save)
                }, Modifier.fillMaxWidth(), enabled = !state.processing)
            }
            1 -> {
                LaunchedEffect(Unit) { if (state.pixCopyPaste == null) vm.preparePix() }
                state.pixCopyPaste?.let { code ->
                    val bmp = remember(code) { PixGenerator.qr(code) }
                    Image(bmp.asImageBitmap(), null, Modifier.size(220.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("PIX copia e cola:", style = MaterialTheme.typography.labelLarge)
                    Text(code, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = {
                        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("pix", code))
                    }, modifier = Modifier.fillMaxWidth()) { Text("Copiar PIX") }
                    Spacer(Modifier.height(16.dp))
                    YellowButton("Já paguei", {
                        state.createdOrderId?.let { nav.navigate(Routes.orderDetail(it)) { popUpTo(Routes.CUSTOMER_HOME) } }
                    }, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

private fun detectBrand(num: String): String {
    val n = num.filter { it.isDigit() }
    return when {
        n.startsWith("4") -> "Visa"
        n.startsWith("5") -> "Mastercard"
        n.startsWith("3") -> "Amex"
        else -> "Cartão"
    }
}
