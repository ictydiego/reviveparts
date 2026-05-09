package br.unasp.reviveparts.ui.screens.customer.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.ui.components.PartCard
import br.unasp.reviveparts.ui.nav.Routes

@Composable
fun HomeScreen(nav: NavController) {
    val ctx = LocalContext.current
    val vm: HomeViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = HomeViewModel.create(ctx.app) as T
    })
    val products by vm.products.collectAsState(initial = emptyList())

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("ReviveParts", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
        Text("Peças impressas em 3D — pronta entrega", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        LazyColumn {
            items(products, key = { it.id }) { p ->
                PartCard(p) { nav.navigate(Routes.partDetail(p.id)) }
            }
        }
    }
}
