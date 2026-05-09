package br.unasp.reviveparts.ui.screens.customer.cart

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.ui.components.YellowButton
import br.unasp.reviveparts.ui.nav.Routes

@Composable
fun CartScreen(nav: NavController, productId: Long, source: String) {
    val ctx = LocalContext.current
    val vm: CartViewModel = viewModel(key = "cart-$productId", factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = CartViewModel.create(ctx.app, productId) as T
    })
    val p by vm.product.collectAsState()
    val product = p ?: return

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Resumo do pedido", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Card { Column(Modifier.padding(16.dp)) {
            Text(product.name, style = MaterialTheme.typography.titleLarge)
            Text("Origem: ${if (source == "AI") "reconhecimento por IA" else "catálogo"}")
            Text("Tempo de prototipagem: ${product.prototypeHours}h")
            Spacer(Modifier.height(8.dp))
            Text("Total: R$ %.2f".format(product.priceCents / 100.0), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
        } }
        Spacer(Modifier.weight(1f))
        YellowButton("Continuar para pagamento", {
            nav.navigate(Routes.payment(product.id, source))
        }, Modifier.fillMaxWidth())
    }
}
