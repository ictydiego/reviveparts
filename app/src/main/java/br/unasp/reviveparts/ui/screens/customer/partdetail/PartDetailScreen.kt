package br.unasp.reviveparts.ui.screens.customer.partdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.domain.model.OrderSource
import br.unasp.reviveparts.ui.components.ModelViewer3D
import br.unasp.reviveparts.ui.components.YellowButton
import br.unasp.reviveparts.ui.nav.Routes

@Composable
fun PartDetailScreen(nav: NavController, id: Long) {
    val ctx = LocalContext.current
    val vm: PartDetailViewModel = viewModel(key = "part-$id", factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = PartDetailViewModel.create(ctx.app, id) as T
    })
    val p by vm.product.collectAsState()
    val product = p ?: return

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        ModelViewer3D(assetPath = product.model3dAsset, modifier = Modifier.fillMaxWidth().height(280.dp))
        Spacer(Modifier.height(16.dp))
        Text(product.name, style = MaterialTheme.typography.headlineMedium)
        Text("R$ %.2f".format(product.priceCents / 100.0), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(product.description)
        Spacer(Modifier.height(8.dp))
        Text(if (product.stockQty > 0) "Em estoque (${product.stockQty} un)" else "${product.prototypeHours}h de prototipagem")
        Spacer(Modifier.height(24.dp))
        YellowButton("Comprar", { nav.navigate(Routes.cart(product.id, OrderSource.CATALOG.name)) }, Modifier.fillMaxWidth())
    }
}
